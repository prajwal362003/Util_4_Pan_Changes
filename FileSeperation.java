package com.sbi.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.stereotype.Component;

import com.sbi.dto.DocumentCount;
import com.sbi.enums.FilePaths;

@Component
public class FileSeperation {

	private static final Logger logger = (Logger) LogManager.getLogger(FileSeperation.class);

	public boolean seperateKycDocs(Path claimedFolder, String lcpcName) throws IOException {
		boolean fileseperationFlag=false;
		
		File zipInputfld = new File(FilePaths.zipInputFolder + "/" + lcpcName + "/" + claimedFolder.getFileName().toString());
		logger.info("zipInputfld  "+zipInputfld.getAbsolutePath());
		if (!(zipInputfld.isDirectory())) {
			zipInputfld.mkdir();
			logger.info("Created a directory  "+zipInputfld.getAbsolutePath());
		}
		
		Path zipInputFolder=Paths.get(zipInputfld.getAbsolutePath());
	//	logger.info("zip Input Folder : "+zipInputFolder);

		List<Path> files=Files.list(claimedFolder).filter(Files::isRegularFile).toList();
		logger.info("No of files found : "+files.size());
		DocumentCount count = countDocuments(files);
		logger.info(count.toString());

		int kyc = 0;
		int kycUpdateForm = 0;
		int poa_poi = 0;

		for(Path filepath : files) {
			logger.info("file path : "+filepath.toString());
			String[] fileName=filepath.getFileName().toString().toLowerCase().split("_");
			if(fileName.length<=1)
				continue;
			if(count.getKyc()>0&&fileName[1].equals("kyc")) {
				copyKycDocuments(filepath,zipInputFolder);
				kyc++;
			}
			/*else if(count.getKycUpdateForm()>0&&fileName[1].equals("kycupdateform")) {
				copyKycDocuments(filepath,zipInputFolder);
				kycUpdateForm++;
			}	*/		
		}
		logger.info("kyc : "+kyc+"     kycUpdateForm : "+kycUpdateForm);
		if(kyc==0) {
			for(Path filepath : files) {
				logger.info("file path : "+filepath.toString());
				String[] fileName=filepath.getFileName().toString().toLowerCase().split("_");
				if(fileName.length<=1)
					continue;
				if((count.getPoa()>0&&fileName[1].equals("poa"))||(count.getPoi()>0&&fileName[1].equals("poi"))) {
					copyKycDocuments(filepath,zipInputFolder);
					poa_poi++;
				}		
			}
		}
		logger.info("poa_poi : "+poa_poi);
//		if ((kyc > 0 || poa_poi > 0) && kycUpdateForm == 1) {
		if ((kyc > 0 || poa_poi > 0)) {
			return true;
		}
		return fileseperationFlag;
	}


	private static void copyKycDocuments(Path filePath, Path destination) throws IOException {
		try{
			Files.createDirectories(destination);
		}catch(FileAlreadyExistsException e) {
			logger.error(e.getLocalizedMessage());
		}
		Path desPath=destination.resolve(filePath.getFileName());
		Files.copy(filePath, desPath, StandardCopyOption.REPLACE_EXISTING);
	}



	private static DocumentCount countDocuments(List<Path> files) {
		DocumentCount count =new DocumentCount();
		int noOfDocuments=0;
		for(Path file : files) {
			logger.info("fileName : "+file.getFileName());

			String[] fileName=file.getFileName().toString().toLowerCase().split("_");
			if(fileName.length<=1) {
				continue;
			}
			if(fileName[1].equals("kyc")) 
				count.incrementKyc();
			else if(fileName[1].equals("kycupdateform"))
				count.incrementKycUpdateForm();
			else if(fileName[1].equals("poa"))
				count.incrementPoa();
			else if(fileName[1].equals("poi"))
				count.incrementPoi();
			else if(fileName[1].equals("others"))
				count.incrementOthers();
			else if(fileName[1].contains("photo"))
				count.incrementPhoto();
			else if(fileName[1].contains("sign")) 
				count.incrementSign();
			
			noOfDocuments++;
		}
		logger.info("No of documents found in the repository : "+noOfDocuments);
		return count;
	}
}
