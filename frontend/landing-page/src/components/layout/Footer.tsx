'use client';

import React from 'react';
import Link from 'next/link';
import { Share2, Mail, Phone, MapPin } from 'lucide-react';

const LinkedinIcon = ({ size = 20 }: { size?: number }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path><rect x="2" y="9" width="4" height="12"></rect><circle cx="4" cy="4" r="2"></circle></svg>
);

const TwitterIcon = ({ size = 20 }: { size?: number }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"></path></svg>
);

const FacebookIcon = ({ size = 20 }: { size?: number }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path></svg>
);

const InstagramIcon = ({ size = 20 }: { size?: number }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg>
);

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
                <FacebookIcon size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <TwitterIcon size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <InstagramIcon size={20} />
              </a>
              <a href="#" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <LinkedinIcon size={20} />
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
