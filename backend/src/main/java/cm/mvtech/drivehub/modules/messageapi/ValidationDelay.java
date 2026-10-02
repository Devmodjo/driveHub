package cm.mvtech.drivehub.modules.messageapi;

/**
 * Délai de traitement annoncé aux utilisateurs pour les validations faites par l'équipe DriveHub
 * (création d'une auto-école, compte administrateur). À modifier ici uniquement : il est repris
 * dans les réponses de l'API et dans les emails. Le frontend affiche le même délai
 * (constante VALIDATION_DELAY dans frontend/src/app/utils/UTILS.ts).
 */
public final class ValidationDelay {

    /** Ex : « sous 48 à 72 heures ». */
    public static final String TEXT = "48 à 72 heures";

    private ValidationDelay() {
    }
}
