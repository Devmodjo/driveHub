'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import type { Dictionary, Locale } from '@/i18n/getDictionary';

interface DictionaryContextProps {
  dict: Dictionary;
  locale: Locale;
  setLocale: (loc: Locale) => void;
}

const DictionaryContext = createContext<DictionaryContextProps | null>(null);

export const useDictionary = () => {
  const context = useContext(DictionaryContext);
  if (!context) {
    throw new Error('useDictionary must be used within a DictionaryProvider');
  }
  return context;
};

export const DictionaryProvider = ({
  children,
  initialDictionary,
  initialLocale,
}: {
  children: React.ReactNode;
  initialDictionary: Dictionary;
  initialLocale: Locale;
}) => {
  // Use client-side state for instantaneous translations without full reloads
  const [locale, setLocaleState] = useState<Locale>(initialLocale);
  const [dict, setDict] = useState<Dictionary>(initialDictionary);

  const setLocale = (newLocale: Locale) => {
    // 1. Save to document cookie so Server Component sees it on reload
    document.cookie = `NEXT_LOCALE=${newLocale}; path=/; max-age=31536000`;
    
    // 2. Dynamically import dictionary and update state immediately (No hard reload needed)
    import(`@/i18n/${newLocale}.json`).then((module) => {
      setDict(module.default);
      setLocaleState(newLocale);
    });
  };

  return (
    <DictionaryContext.Provider value={{ dict, locale, setLocale }}>
      {children}
    </DictionaryContext.Provider>
  );
};
