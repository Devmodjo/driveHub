'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Share2, Menu, X, Sun, Moon, Globe } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from 'next-themes';
import { useDictionary } from '@/components/DictionaryProvider';

export const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const { theme, setTheme, systemTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const { dict, locale, setLocale } = useDictionary();

  useEffect(() => {
    setMounted(true);
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { name: dict.Navbar.link_features, href: '#features' },
    { name: dict.Navbar.link_pricing, href: '#pricing' },
    { name: dict.Navbar.link_about, href: '#about' },
    { name: dict.Navbar.link_faq, href: '#faq' },
  ];

  const currentTheme = theme === 'system' ? systemTheme : theme;

  return (
    <nav
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled ? 'bg-white/80 dark:bg-black/80 backdrop-blur-md border-b border-black/10 dark:border-white/10 py-3' : 'bg-transparent py-5'
      }`}
    >
      <div className="container mx-auto px-6 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2 group">
          <div className="w-10 h-10 bg-[#0070f3] rounded-xl flex items-center justify-center text-white transition-transform group-hover:rotate-12">
            <Share2 size={24} />
          </div>
          <span className="text-2xl font-bold tracking-tight text-black dark:text-white">DriveHub</span>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <Link
              key={link.name}
              href={link.href}
              className="text-black/70 dark:text-white/70 hover:text-[#0070f3] dark:hover:text-[#0070f3] transition-colors font-medium"
            >
              {link.name}
            </Link>
          ))}
        </div>

        <div className="hidden md:flex items-center gap-4">
          <button
            onClick={() => setLocale(locale === 'fr' ? 'en' : 'fr')}
            className="flex items-center gap-2 p-2 px-3 rounded-full hover:bg-black/5 dark:hover:bg-white/5 transition-colors text-black dark:text-white font-medium text-sm"
          >
            <Globe size={18} />
            {locale.toUpperCase()}
          </button>
          
          {mounted && (
            <button
              onClick={() => setTheme(currentTheme === 'dark' ? 'light' : 'dark')}
              className="p-2 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 transition-colors text-black dark:text-white"
            >
              {currentTheme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
            </button>
          )}
          <Button variant="ghost" className="text-black dark:text-white hover:bg-black/5 dark:hover:bg-white/5 font-medium">{dict.Navbar.btn_login}</Button>
          <Button className="bg-[#0070f3] hover:bg-[#0070f3]/90 text-white shadow-lg shadow-[#0070f3]/25 border-none font-bold rounded-full px-6">{dict.Navbar.btn_signup}</Button>
        </div>

        {/* Mobile Menu Button */}
        <div className="flex md:hidden items-center gap-4">
          {mounted && (
            <button
              onClick={() => setTheme(currentTheme === 'dark' ? 'light' : 'dark')}
              className="p-2 rounded-lg bg-black/5 dark:bg-white/5 text-black dark:text-white"
            >
              {currentTheme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
            </button>
          )}
          <button className="text-black dark:text-white" onClick={() => setIsOpen(!isOpen)}>
            {isOpen ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>
      </div>

      {/* Mobile Nav */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="md:hidden bg-white dark:bg-black border-b border-black/10 dark:border-white/10 overflow-hidden"
          >
            <div className="container mx-auto px-6 py-6 flex flex-col gap-4">
              {navLinks.map((link) => (
                <Link
                  key={link.name}
                  href={link.href}
                  className="text-lg font-medium py-2 text-black dark:text-white"
                  onClick={() => setIsOpen(false)}
                >
                  {link.name}
                </Link>
              ))}
              <div className="flex flex-col gap-3 mt-4">
                <button
                  onClick={() => { setLocale(locale === 'fr' ? 'en' : 'fr'); setIsOpen(false); }}
                  className="flex justify-center items-center gap-2 p-3 bg-black/5 dark:bg-white/5 rounded-xl font-bold text-black dark:text-white"
                >
                  <Globe size={18} /> {locale.toUpperCase()}
                </button>
                <Button variant="outline" className="w-full text-black border-black/20 dark:text-white dark:border-white/20 font-medium">{dict.Navbar.btn_login}</Button>
                <Button className="w-full bg-[#0070f3] text-white font-bold rounded-full py-6">{dict.Navbar.btn_signup}</Button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};
