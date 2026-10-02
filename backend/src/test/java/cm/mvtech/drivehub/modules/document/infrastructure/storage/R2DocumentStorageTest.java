package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Appels envoyés à Cloudflare R2 (API compatible S3), sans réseau : le client S3 est simulé. */
class R2DocumentStorageTest {

    private final S3Client client = mock(S3Client.class);
    private final R2DocumentStorage storage = new R2DocumentStorage(client, "drivehub-documents");

    @Test
    void put_SendsOpaqueObjectToBucket() {
        storage.put("documents/x", new byte[]{9});
        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(request.capture(), any(RequestBody.class));
        assertEquals("drivehub-documents", request.getValue().bucket());
        assertEquals("documents/x", request.getValue().key());
        assertEquals("application/octet-stream", request.getValue().contentType());
    }

    @Test
    void get_ReturnsBytes_And404WhenMissing() {
        when(client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[]{7}));
        assertArrayEquals(new byte[]{7}, storage.get("documents/x"));

        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertThrows(ResourceNotFoundException.class, () -> storage.get("documents/y"));
    }

    @Test
    void delete_DeletesObject() {
        storage.delete("documents/x");
        verify(client).deleteObject(DeleteObjectRequest.builder().bucket("drivehub-documents").key("documents/x").build());
    }
}
