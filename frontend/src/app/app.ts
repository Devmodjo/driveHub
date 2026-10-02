import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './services/theme-service/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // Instancié dès le démarrage : applique le thème clair / sombre mémorisé sur toutes les pages
  private readonly theme = inject(ThemeService);
}
