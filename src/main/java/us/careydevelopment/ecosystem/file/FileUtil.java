package us.careydevelopment.ecosystem.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import us.careydevelopment.ecosystem.file.exception.FileTooLargeException;
import us.careydevelopment.ecosystem.file.exception.ImageRetrievalException;
import us.careydevelopment.ecosystem.file.exception.MissingFileException;

public abstract class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);
    
    protected static String PROFILE_DIR = "profile";
    
    protected String userFilesBasePath;
    protected Long maxFileUploadSize;

    
    public Path fetchProfilePhotoById(String id) throws ImageRetrievalException {
        Path imagePath = null;
        
        Path rootLocation = Paths.get(getRootLocationForUserProfileImageUpload(id));
        LOG.debug("Fetching profile image from " + rootLocation.toString());

        try {
            if (rootLocation.toFile().exists()) {
                Iterator<Path> iterator = Files.newDirectoryStream(rootLocation).iterator();
                
                if (iterator.hasNext()) {
                    imagePath = iterator.next();                
                    LOG.debug("File name is " + imagePath);
                }            
            }
        } catch (IOException ie) {
            throw new ImageRetrievalException(ie.getMessage());
        }
        
        return imagePath;
    }
    
    
    protected void deleteAllFilesInDirectory(Path rootLocation) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootLocation)) {
            directoryStream.forEach(path -> {
                path.toFile().delete();
            });
        } catch (IOException ie) {
            LOG.error("Problem trying to delete files in " + rootLocation.toString());
        }
    }

    
    protected void validateFile(MultipartFile file, Long maxFileUploadSize) throws MissingFileException, FileTooLargeException {
        checkFileExistence(file);
        checkFileSize(file, maxFileUploadSize);
    }
        
       
    public void checkFileSize(MultipartFile file, Long maxFileUploadSize) throws FileTooLargeException {
        if (file.getSize() > maxFileUploadSize) {
            String message = "File is too large - max size is " + maxFileUploadSize;
            throw new FileTooLargeException(message);
        }
    }

    
    public void checkFileExistence(MultipartFile file) throws MissingFileException {
        if (file == null) throw new MissingFileException("No file sent!");
        if (StringUtils.isEmpty(file.getName())) throw new MissingFileException("No file sent!");
    }
    
    
    protected void createDirectoryIfItDoesntExist(String dir) {
        final Path path = Paths.get(dir);
        
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ie) {
                LOG.error("Problem creating directory " + dir);
            }
        } 
    }
    
    
    public String properSeparators(String filePath) {
        if (filePath != null) {
            String properPath = filePath.replaceAll("\\\\", "/");
            return properPath;
        } else {
            return null;
        }
    }

    
    public String getRootLocationForUserUpload(String userId) {
        if (StringUtils.isEmpty(userId)) throw new IllegalArgumentException("No user id!");
        
        StringBuilder builder = new StringBuilder();
        
        builder.append(userFilesBasePath);
        builder.append("/");
        builder.append(userId);
        
        String location = builder.toString();
        
        createDirectoryIfItDoesntExist(location);
        
        return location;
    }
    
        
    public String getRootLocationForUserProfileImageUpload(String userId) {
        if (StringUtils.isEmpty(userId)) throw new IllegalArgumentException("No user id!");

        String base = getRootLocationForUserUpload(userId);
        
        StringBuilder builder = new StringBuilder(base);
        builder.append("/");
        builder.append(PROFILE_DIR);
        
        String location = builder.toString();
        
        createDirectoryIfItDoesntExist(location);
        
        return location;
    }   
}
