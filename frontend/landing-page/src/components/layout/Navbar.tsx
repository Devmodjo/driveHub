'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Share2, Menu, X, Sun, Moon } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from 'next-themes';

export const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const { theme, setTheme, systemTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { name: 'Fonctionnalités', href: '#features' },
    { name: 'Tarifs', href: '#pricing' },
    { name: 'À propos', href: '#about' },
    { name: 'FAQ', href: '#faq' },
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
          {mounted && (
            <button
              onClick={() => setTheme(currentTheme === 'dark' ? 'light' : 'dark')}
              className="p-2 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 transition-colors text-black dark:text-white"
            >
              {currentTheme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
            </button>
          )}
          <Button variant="ghost" className="text-black dark:text-white hover:bg-black/5 dark:hover:bg-white/5">Connexion</Button>
          <Button className="bg-[#0070f3] hover:bg-[#0070f3]/90 text-white shadow-[0_0_15px_rgba(0,112,243,0.3)] border-none">Démarrer gratuitement</Button>
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
                <Button variant="outline" className="w-full text-black border-black/20 dark:text-white dark:border-white/20">Connexion</Button>
                <Button className="w-full bg-[#0070f3] text-white">Démarrer gratuitement</Button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};
