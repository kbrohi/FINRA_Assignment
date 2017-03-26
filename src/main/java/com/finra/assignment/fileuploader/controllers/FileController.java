package com.finra.assignment.fileuploader.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.finra.assignment.fileuploader.beans.File;
import com.finra.assignment.fileuploader.repository.FileRepository;
import com.finra.assignment.fileuploader.repository.redisdictionary.RedisKeywordDataRepository;
import com.finra.assignment.fileuploader.services.filesystem.FileSystemStorageService;
import com.finra.assignment.fileuploader.services.filesystem.StorageException;
import com.finra.assignment.fileuploader.services.filesystem.StorageFileNotFoundException;
import com.finra.assignment.fileuploader.services.keywordextractor.FileKeyWordAndIdExtractor;

import java.util.List;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 *
 */

/**
 * Provide the implementation fo the rest endpoints
 */

@RestController
public class FileController {

	private final FileRepository fileRepository;

	private	final FileSystemStorageService fileSystemStorageService;

	private final RedisKeywordDataRepository redisKeywordDataRepository;

	private final FileKeyWordAndIdExtractor fileKeyWordAndIdExtractor;

	/**
	 *
	 * @param fileRepository The file meta redis repo
	 * @param fileSystemStorageService Service used to store uploaded files on the filesystem
	 * @param redisKeywordDataRepository The redis repository used to store keywords extracted from file meta
	 * @param fileKeyWordAndIdExtractor	The extractor service used to extract keywords from file meta
	 */
	@Autowired
	public FileController(FileRepository fileRepository, FileSystemStorageService fileSystemStorageService,
						  RedisKeywordDataRepository redisKeywordDataRepository,
						  FileKeyWordAndIdExtractor fileKeyWordAndIdExtractor) {

		this.fileRepository = fileRepository;
		this.fileSystemStorageService = fileSystemStorageService;
		this.redisKeywordDataRepository = redisKeywordDataRepository;
		this.fileKeyWordAndIdExtractor = fileKeyWordAndIdExtractor;

		fileRepository.deleteAll();
	}

	/**
	 * Provide the endpoint to upload file to the server
	 * @param file The file uploaded
	 * @param description The description of the file
	 * @return an http response
	 */
	@PostMapping("/upload")
	@ResponseBody
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
							 @RequestParam("description") String description){

		try {
			System.out.println(file.getOriginalFilename());
			File fileMeta = new File(file.getOriginalFilename(), description, true);
			fileMeta = fileRepository.save(fileMeta);
			fileSystemStorageService.saveFile(file, fileMeta.getId() + file.getOriginalFilename()
					.substring(file.getOriginalFilename().length()-4));
			redisKeywordDataRepository.indexDataByKeywords("fileIndex", fileKeyWordAndIdExtractor, fileMeta);
			return ResponseEntity.ok().body("File saved successfully");
		}catch (StorageException ex){
			ex.printStackTrace();
			return  ResponseEntity.badRequest().body("Error while saving file");
		}
	}

	/**
	 * Provide an endpoint to search for files
	 * @param keyword The search keyword
	 * @return The list of file's keys corresponding to the search
	 */
	@GetMapping("/search")
	@ResponseBody
	public List<String> searchFile(@RequestParam(value = "keyword") String keyword){

		return redisKeywordDataRepository.findDataByPhrase("fileIndex", keyword);
	}

	/**
	 * Provide an endpoint to download files by their names. Will return one file even multiple files with the same name
	 * were uploaded. To upload a specific file user should do a search (description might be used as keyword) of it an
	 * get it by id.
	 * @param filename The file name
	 * @return A response body containing the file and the status of the request
	 */
	@GetMapping("/getByName")
	@ResponseBody
	public ResponseEntity<Resource> getFileByName(@RequestParam(value = "name") String filename) {

		try{
			List<File> files = fileRepository.findByName(filename);
			System.out.println(files.size());
			if(files.isEmpty()){
				return ResponseEntity.notFound().build();
			}else{
				Resource file = fileSystemStorageService.getFileAsResource(files.get(0).getId() +
						files.get(0).getName().substring(files.get(0).getName().length() - 4));
				return ResponseEntity
						.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
						.body(file);
			}

		}catch (StorageFileNotFoundException ex){
			return ResponseEntity.notFound().build();
		}

	}

	/**
	 * Provide an endpoint to download files by their ids
	 * @param id The id of the file
	 * @return A response body containing the file and the status of the request
	 */
	@GetMapping("/getById")
	@ResponseBody
	public ResponseEntity<Resource> getFileById(@RequestParam(value = "id") String id) {

		String name = fileRepository.findOne(id).getName();
		try {
			Resource file = fileSystemStorageService.getFileAsResource(id + name.substring(name.length() - 4));
			return ResponseEntity
					.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
					.body(file);

		}catch (StorageFileNotFoundException ex){
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Provide an endpoint to get the meta data of files by their name
	 * @param filename The name of the file
	 * @return return a json object representation of the file meta
	 */
	@GetMapping("/getFileMetaByName")
	@ResponseBody
	public List<File> getFileMetaByName(@RequestParam(value = "name") String filename) {
		return fileRepository.findByName(filename);
	}

	/**
	 * Provide an endpoint to get the meta data of files by their name
	 * @param id The id of the file
	 * @return return a json object representation of the file meta
	 */
	@GetMapping("/getFileMetaById")
	@ResponseBody
	public Object getFileMetaById(@RequestParam(value = "id") String id) {
		File file = fileRepository.findOne(id);
		if(file != null){
			return file;
		}else {
			return ResponseEntity.notFound().build();
		}
	}
}
