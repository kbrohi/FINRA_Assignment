package com.finra.assignment.fileuploader.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.finra.assignment.fileuploader.beans.File;
import com.finra.assignment.fileuploader.repository.FileRepository;
import com.finra.assignment.fileuploader.services.filesystem.FileSystemStorageService;
import com.finra.assignment.fileuploader.services.filesystem.StorageFileNotFoundException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
 
/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 *  Service for sending e-mail notifications containing the list of newly added files
 */
@Service
@PropertySource("classpath:/application.yml")
public class NewFilesNotificationService {

    private JavaMailSender javaMailSender;
    private FileRepository fileRepository;
    private FileSystemStorageService fileSystemStorageService;
    private List<File> newlyAddedFiles;

    private final long NOTIFICATION_INTERVAL = 100000; // 10 Minutes

    @Value("${notification.to}")
    private String to;

    @Value("${notification.from}")
    private String from;

    @Value("${notification.subject}")
    private String subject;

    @Value("${notification.text}")
    private String text;

    /**
     *
     * @param javaMailSender A java mail sender automatically generate by spring boot mail library
     * @param fileRepository The file repo
     * @param fileSystemStorageService The file system storage service
     */
    @Autowired
    public NewFilesNotificationService(JavaMailSender javaMailSender, FileRepository fileRepository,
                                       FileSystemStorageService fileSystemStorageService){
        this.javaMailSender = javaMailSender;
        this.fileRepository = fileRepository;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    /**
     * Send notification emails of the list of newly added files (when available) at a fixed interval
     * @throws MailException exception
     * @throws InterruptedException exception
     * @throws MessagingException exception
     */
    @Scheduled(fixedDelay = NOTIFICATION_INTERVAL)
    public void sendNotificationEmail() throws MailException, InterruptedException, MessagingException {

        Resource listOfNewlyAddedFiles = getListOfNewlyAddedFilesAsResource();

        if(listOfNewlyAddedFiles != null){
            System.out.println("Sending email...");
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(text);
            helper.addAttachment(listOfNewlyAddedFiles.getFilename(), listOfNewlyAddedFiles);

            javaMailSender.send(mail);


            for (File newlyAddedFile : newlyAddedFiles) {
                newlyAddedFile.setNewlyAdded(false);
            }
            fileRepository.save(newlyAddedFiles);
            System.out.println("Email Sent!");
            System.out.println(newlyAddedFiles);
        }
    }

    //Load the csv file containing the list of newly added files as a resource
    private Resource getListOfNewlyAddedFilesAsResource(){
        Path file = createListOfNewlyAddedFiles();
        if(file != null){
            try {
                Resource resource = new UrlResource(file.toUri());
                if(resource.exists() || resource.isReadable()) {
                    return resource;
                }
            } catch (MalformedURLException e) {
                new StorageFileNotFoundException("Could not read file: " + file.getFileName(), e).printStackTrace();
            }
        }
        return null;
    }

    //Get the list of newly added files from the redis data store, creates as csv files containing this list and returns
    //a Path object representing the file path of the this file
    private Path createListOfNewlyAddedFiles(){

        newlyAddedFiles = fileRepository.findByIsNewlyAdded(true);

        if(newlyAddedFiles.isEmpty()){
            return null;
        }
        Path listOfNewlyAddedFilePath = fileSystemStorageService.load("newly-added-files.csv");

        CSVPrinter csvPrinter;


        try {
            CSVFormat csvFormat =  CSVFormat.DEFAULT.withHeader("Id", "Name", "Description");

            if(listOfNewlyAddedFilePath.toFile().exists()){
                System.out.println("Deletion of newly-added-files.csv" + listOfNewlyAddedFilePath.toFile().delete());
            }
            FileWriter fileWriter = new FileWriter(listOfNewlyAddedFilePath.toFile());

            csvPrinter = new CSVPrinter(fileWriter, csvFormat);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        for (File file : newlyAddedFiles) {

            try {
                ArrayList<String> fileRecord = new ArrayList<>();
                fileRecord.add(file.getId());
                fileRecord.add(file.getName());
                fileRecord.add(file.getDescription());
                csvPrinter.printRecord(fileRecord);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            csvPrinter.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

        return listOfNewlyAddedFilePath;

    }
}
