package com.pharmacr.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnBean(Storage.class)
public class FirebaseStorageService {

    @Value("${firebase.bucket.name}")
    private String bucketName;

    @Value("${firebase.storage.path}")
    private String storagePath;

    private final Storage storage;

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    //Sube un archivo de imagen al almacenamiento de Firebase
    public String subirImagen(MultipartFile archivo, String carpeta, Integer id) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        //Se genera el nombre del archivo con un formato consistente
        String nombreArchivo = "img" + numeroConFormato(id) + extension;

        File temporal = convertirAArchivo(archivo);

        try {
            return subirAFirebase(temporal, carpeta, nombreArchivo);
        } finally {
            //Se elimina siempre el archivo temporal, haya fallado o no la subida
            if (temporal.exists()) {
                temporal.delete();
            }
        }
    }

   
    private File convertirAArchivo(MultipartFile multipartFile) throws IOException {
        File temporal = File.createTempFile("upload-", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(temporal)) {
            fos.write(multipartFile.getBytes());
        }
        return temporal;
    }


    private String subirAFirebase(File archivo, String carpeta, String nombreArchivo) throws IOException {
        BlobId blobId = BlobId.of(bucketName, storagePath + "/" + carpeta + "/" + nombreArchivo);
        String mimeType = Files.probeContentType(archivo.toPath());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(mimeType != null ? mimeType : "media")
                .build();

        storage.create(blobInfo, Files.readAllBytes(archivo.toPath()));

        
        return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
    }
    private String numeroConFormato(long id) {
        return String.format("%014d", id);
    }
}
