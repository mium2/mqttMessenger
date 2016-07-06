package com.msp.messenger.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadUtil {
	protected static Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);
	
	public static com.msp.messenger.util.UploadedFile uploadMultipartFile(MultipartFile mpFile,
			String realPath, boolean chooseOriFileName) {
		InputStream stream;
		
		String tempFileName = null;
		
		if(chooseOriFileName){
			tempFileName = mpFile.getOriginalFilename();
		}else{
			UUID uuid = UUID.randomUUID();
			tempFileName = uuid.toString();
		}
		
		if(!realPath.endsWith("/")){
			realPath += "/";
		}
		
		File targetDir = new File(realPath);
		if(!targetDir.exists()) targetDir.mkdirs();
		
		try {
			stream = mpFile.getInputStream();

			OutputStream bos = new FileOutputStream(realPath + tempFileName);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}
			bos.close();
			stream.close();

			if (logger.isDebugEnabled()) {
				logger.debug("The file has been written to \"" + realPath
						+ tempFileName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// File file = new File(realPath + tempFileName);
		// BoardFile boardFile = new BoardFile();
		// boardFile.setFileName(mpFile.getOriginalFilename());
		// boardFile.setFileSize(mpFile.getSize());
		// boardFile.setContentType(mpFile.getContentType());
		// boardFile.setTempFileName(tempFileName);
		com.msp.messenger.util.UploadedFile file = new com.msp.messenger.util.UploadedFile();
		file.setFilename(mpFile.getOriginalFilename());
		file.setTempFilename(tempFileName);
		file.setSize(mpFile.getSize());
		file.setContentType(mpFile.getContentType());

		return file;
	}
}
