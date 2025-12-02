import { bootstrapApplication } from '@angular/platform-browser';
import { APP_INITIALIZER, enableProdMode, importProvidersFrom } from '@angular/core';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { BsModalService } from 'ngx-bootstrap/modal';

import { AppComponent } from './app/app.component';
import { AppRoutingModule } from './app/app-routing.module';
import { environment } from './environments/environment';
import { Configuration, ApiModule } from './app/generated';
import { SsService } from './app/services/ss.service';
import { ErrorInterceptor } from './app/utils/error.interceptor';
import { TokenInterceptor } from './app/utils/token.interceptor';

if (environment.production) {
  enableProdMode();
}

export const getConfiguration = () => {
  return new Configuration({
    basePath: environment.basePath,
  });
};

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi()
    ),

    provideAnimations(),

    provideToastr({
      timeOut: 10000,
      positionClass: 'toast-top-right',
      preventDuplicates: true
    }),

    importProvidersFrom(
      AppRoutingModule,
      ApiModule.forRoot(getConfiguration)
    ),

    BsModalService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    },
    SsService,
    {
      provide: APP_INITIALIZER,
      useFactory: (ds: SsService) => () => ds.init(),
      deps: [SsService],
      multi: true
    }
  ]
}).catch((err) => console.error(err));
