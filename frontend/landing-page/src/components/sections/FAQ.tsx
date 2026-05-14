'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown } from 'lucide-react';

const faqs = [
  {
    question: 'Est-ce que DriveHub fonctionne sans connexion internet ?',
    answer: 'DriveHub est une plateforme cloud. Cependant, l\'application est optimisée pour fonctionner avec une très faible bande passante, ce qui est idéal pour les zones avec une couverture réseau instable au Cameroun.',
  },
  {
    question: 'Comment se passe l\'intégration du Mobile Money ?',
    answer: 'Nous supportons nativement Orange Money et MTN Mobile Money. Vous pouvez configurer vos comptes directement dans votre espace pour recevoir les frais d\'inscription en toute sécurité et fluidité.',
  },
  {
    question: 'Les étudiants peuvent-ils réviser le code en ligne ?',
    answer: 'Oui, nous proposons une base de tests conformes au code de la route camerounais, avec des explications claires et un suivi des progrès en temps réel pour l\'élève.',
  },
  {
    question: 'Mes données sont-elles vraiment protégées ?',
    answer: 'Absolument. Nous utilisons des serveurs sécurisés avec des sauvegardes régulières. Vos informations sont chiffrées selon les standards de sécurité modernes.',
  },
];

export const FAQ = () => {
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  return (
    <section id="faq" className="py-24 bg-white dark:bg-black">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">Support</h2>
          <h3 className="font-display text-4xl md:text-5xl font-black text-black dark:text-white mb-6">Questions Fréquentes</h3>
          <p className="text-xl text-black/60 dark:text-white/60 font-light">
            Tout ce que vous devez savoir pour démarrer sereinement avec DriveHub.
          </p>
        </div>

        <div className="max-w-3xl mx-auto space-y-4">
          {faqs.map((faq, index) => (
            <div
              key={index}
              className="bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 rounded-2xl overflow-hidden transition-all duration-300 hover:border-[#0070f3]/30"
            >
              <button
                className="w-full px-6 py-6 flex items-center justify-between text-left group"
                onClick={() => setActiveIndex(activeIndex === index ? null : index)}
              >
                <span className="font-display font-black text-lg text-black dark:text-white group-hover:text-[#0070f3] transition-colors">{faq.question}</span>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center bg-black/5 dark:bg-white/5 shrink-0 transition-transform duration-300 ${activeIndex === index ? 'rotate-180 bg-[#0070f3] text-white' : 'text-black/50 dark:text-white/50'}`}>
                  <ChevronDown size={18} />
                </div>
              </button>
              
              <AnimatePresence>
                {activeIndex === index && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                  >
                    <div className="px-6 pb-6 pt-2 text-black/60 dark:text-white/60 text-base leading-relaxed font-light">
                      {faq.answer}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
