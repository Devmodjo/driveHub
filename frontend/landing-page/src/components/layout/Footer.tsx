'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Mail, Phone, MapPin } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

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
  const { dict } = useDictionary();
  return (
    <footer className="bg-slate-900 text-slate-300 pt-20 pb-10">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 mb-16">
          <div className="space-y-6">
            <Link href="/" className="flex items-center gap-3 group transition-transform hover:scale-[1.02] active:scale-95">
              <div className="relative w-12 h-12 rounded-xl overflow-hidden shadow-lg shadow-[#0070f3]/20 border border-white/10 bg-black">
                <Image
                  src="/dh_icon.png"
                  alt="DH Icon"
                  fill
                  className="object-cover"
                />
              </div>
              <div className="flex flex-col -gap-1">
                <span className="text-2xl font-black italic tracking-tighter leading-none">
                  <span className="text-[#0070f3]">Drive</span>
                  <span className="text-white" style={{ WebkitTextStroke: '1px white', color: 'transparent' }}>Hub</span>
                </span>
                <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#0070f3]/80 leading-none">Management Hub</span>
              </div>
            </Link>
            <p className="text-slate-400 leading-relaxed font-light">
              {dict.Footer.description}
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
            <h4 className="text-white font-bold mb-6 text-lg">{dict.Footer.quick_links}</h4>
            <ul className="space-y-4 font-light">
              <li><Link href="#features" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_features}</Link></li>
              <li><Link href="#pricing" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_pricing}</Link></li>
              <li><Link href="#about" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_about}</Link></li>
              <li><Link href="#faq" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_faq}</Link></li>
              <li><Link href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_blog}</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6 text-lg">{dict.Footer.help_legal}</h4>
            <ul className="space-y-4 font-light">
              <li><a href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_help}</a></li>
              <li><a href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_terms}</a></li>
              <li><a href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_privacy}</a></li>
              <li><a href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_security}</a></li>
              <li><a href="#" className="hover:text-[#0070f3] transition-colors">{dict.Footer.link_contact}</a></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6 text-lg">{dict.Footer.contact_us}</h4>
            <ul className="space-y-4">
              <li className="flex items-start gap-3 font-light">
                <Mail size={20} className="text-[#0070f3] mt-1 shrink-0" />
                <span>contact@drivehub.cm</span>
              </li>
              <li className="flex items-start gap-3 font-light">
                <Phone size={20} className="text-[#0070f3] mt-1 shrink-0" />
                <span>+237 6XX XX XX XX</span>
              </li>
              <li className="flex items-start gap-3 font-light">
                <MapPin size={20} className="text-[#0070f3] mt-1 shrink-0" />
                <span>{dict.Footer.address}</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-800 text-center text-sm text-slate-500 font-light tracking-wide">
          <p>© {new Date().getFullYear()} DriveHub. {dict.Footer.copyright} <span className="text-white font-bold">MV-TECH</span>.</p>
        </div>
      </div>
    </footer>
  );
};
