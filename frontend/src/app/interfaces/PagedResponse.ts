/**
 * Modèle générique d'une réponse paginée retournée par l'API.
 *
 * Utilisation : PagedResponse<AdminProfile>, PagedResponse<DrivingSchool>, etc.
 *
 * @template T - Le type des éléments contenus dans la page
 */
export interface PagedResponse<T> {
  /** Les éléments de la page courante */
  content: T[];

  /** Numéro de la page courante (commence à 0) */
  number: number;

  /** Nombre d'éléments par page */
  size: number;

  /** Nombre total d'éléments sur toutes les pages */
  totalElements: number;

  /** Nombre total de pages disponibles */
  totalPages: number;

  /** Vrai si c'est la première page */
  first: boolean;

  /** Vrai si c'est la dernière page */
  last: boolean;

  /** Vrai si la page ne contient aucun élément */
  empty: boolean;
}
