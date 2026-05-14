import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient} from '@angular/common/http';
import { routes } from './app.routes';

/**
 * fichier de configuration global de l'application
 * on fournie le provider du HttpClient afin de pouvoir l'utiliser
 * dans l'app lors des appel apis
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient()
  ]
};
