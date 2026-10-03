package cm.mvtech.drivehub.core.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Chiffre automatiquement une colonne texte lors de l'écriture en base, et la déchiffre à la lecture.
 *
 * Utilisation dans une entité :
 * <pre>
 *   &#64;Convert(converter = EncryptedStringConverter.class)
 *   private String documentNumber;
 * </pre>
 * Le code Java manipule la valeur en clair ; la base ne contient que « v1:... ».
 * Attention : une colonne chiffrée ne peut pas être recherchée avec WHERE (utiliser une empreinte, voir
 * {@link DataEncryptionService#blindIndex}).
 */
@Component
@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final DataEncryptionService encryption;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryption.encryptString(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryption.decryptString(dbData);
    }
}
