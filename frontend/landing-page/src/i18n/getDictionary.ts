import fr from './fr.json';
import en from './en.json';

export type Dictionary = typeof fr;
export type Locale = 'fr' | 'en';

export const dictionaries = {
  fr,
  en,
};

export const getDictionary = (locale: Locale): Dictionary => {
  return dictionaries[locale] ?? dictionaries.fr;
};
