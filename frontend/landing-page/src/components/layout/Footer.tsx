'use client';

import React from 'react';
import Link from 'next/link';
import { Share2, Facebook, Twitter, Instagram, Linkedin, Mail, Phone, MapPin } from 'lucide-react';

export const Footer = () => {
  return (
    <footer className="bg-slate-900 text-slate-300 pt-20 pb-10">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 mb-16">
          <div className="space-y-6">
            <Link href="/" className="flex items-center gap-2 group">
              <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center text-white">
                <Share2 size={24} />
              </div>
              <span className="text-2xl font-bold tracking-tight text-white">DriveHub</span>
            </Link>
            <p className="text-slate-400 leading-relaxed">
              Le premier hub digital de gestion pour les auto-écoles en Afrique. Simplifiez vos opérations et boostez votre croissance.
            </p>
            <div className="flex gap-4">
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <Facebook size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <Twitter size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <Instagram size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <Linkedin size={20} />
              </a>
            </div>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6 text-lg">Liens Rapides</h4>
            <ul className="space-y-4">
              <li><Link href="#features" className="hover:text-primary transition-colors">Fonctionnalités</Link></li>
              <li><Link href="#pricing" className="hover:text-primary transition-colors">Tarifs</Link></li>
              <li><Link href="#about" className="hover:text-primary transition-colors">À propos</Link></li>
              <li><Link href="#faq" className="hover:text-primary transition-colors">FAQ</Link></li>
              <li><Link href="#" className="hover:text-primary transition-colors">Blog</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6 text-lg">Support & Légal</h4>
            <ul className="space-y-4">
              <li><a href="#" className="hover:text-primary transition-colors">Centre d'aide</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Conditions d'utilisation</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Politique de confidentialité</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Sécurité des données</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Contact</a></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6 text-lg">Nous Contacter</h4>
            <ul className="space-y-4">
              <li className="flex items-start gap-3">
                <Mail size={20} className="text-primary mt-1 shrink-0" />
                <span>contact@drivehub.afrique</span>
              </li>
              <li className="flex items-start gap-3">
                <Phone size={20} className="text-primary mt-1 shrink-0" />
                <span>+225 07 00 00 00 00</span>
              </li>
              <li className="flex items-start gap-3">
                <MapPin size={20} className="text-primary mt-1 shrink-0" />
                <span>Plateau, Abidjan, Côte d'Ivoire</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-800 text-center text-sm text-slate-500">
          <p>© {new Date().getFullYear()} DriveHub. Tous droits réservés. Propulsé par la Tech Africaine.</p>
        </div>
      </div>
    </footer>
  );
};
