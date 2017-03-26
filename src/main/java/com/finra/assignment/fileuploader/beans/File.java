package com.finra.assignment.fileuploader.beans;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 *
 */

// Creates a keyspace called files to store file's meta information as redis hashes
@RedisHash("files")

//Annotation from the Lombok library that permits the auto generation of equals and hashcode method for the class
@EqualsAndHashCode

//Annotation from the Lombok library that permits the auto generation a no argument constructor
@NoArgsConstructor

public class File {

    /**
     *
     * @param name The name of the file
     * @param description The description of the file
     * @param isNewlyAdded Specifies whether the file is newly added or not
     */
    public File(String name, String description, boolean isNewlyAdded){
        this.name = name;
        this.description = description;
        this.isNewlyAdded = isNewlyAdded;
    }

    @Id
    private String id;

    @Indexed
    private String name;

    @Indexed
    private String description;

    @Indexed
    private boolean isNewlyAdded;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNewlyAdded() {
        return isNewlyAdded;
    }

    public void setNewlyAdded(boolean newlyAdded) {
        isNewlyAdded = newlyAdded;
    }
}
