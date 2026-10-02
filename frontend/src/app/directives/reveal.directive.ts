import { Directive, ElementRef, OnDestroy, OnInit, inject, input } from '@angular/core';

export type RevealEffect = 'up' | 'down' | 'left' | 'scale';

/**
 * Animation d'apparition au défilement — remplace les <motion.div initial/whileInView> de framer-motion.
 *
 * Utilisation :
 *   <div appReveal>...</div>                                  (fondu + montée de 20px)
 *   <h2 appReveal="left" [revealDelay]="100">...</h2>         (fondu depuis la gauche, 100 ms de délai)
 *
 * L'élément démarre invisible (classe .reveal), puis reçoit .reveal-visible dès qu'il entre
 * à l'écran (IntersectionObserver). L'animation ne se joue qu'une fois (viewport={{ once: true }}).
 * Les styles sont dans src/styles.css.
 */
@Directive({ selector: '[appReveal]' })
export class RevealDirective implements OnInit, OnDestroy {
  readonly appReveal = input<RevealEffect | ''>('up');
  /** Délai avant l'animation, en millisecondes. */
  readonly revealDelay = input(0);
  /** Durée de l'animation, en millisecondes. */
  readonly revealDuration = input(800);

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private observer?: IntersectionObserver;

  ngOnInit(): void {
    this.element.classList.add('reveal', `reveal-${this.appReveal() || 'up'}`);
    this.element.style.setProperty('--reveal-delay', `${this.revealDelay()}ms`);
    this.element.style.setProperty('--reveal-duration', `${this.revealDuration()}ms`);

    if (typeof IntersectionObserver === 'undefined') {
      this.element.classList.add('reveal-visible');
      return;
    }
    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          this.element.classList.add('reveal-visible');
          this.observer?.disconnect();
        }
      },
      { threshold: 0.1 },
    );
    this.observer.observe(this.element);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
