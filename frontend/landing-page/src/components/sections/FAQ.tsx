'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, ChevronUp } from 'lucide-react';

const faqs = [
  {
    question: 'Est-ce que DriveHub fonctionne sans connexion internet ?',
    answer: 'DriveHub est une plateforme cloud qui nécessite une connexion internet pour la synchronisation en temps réel. Cependant, nous optimisons l\'application pour qu\'elle soit légère et rapide, même avec une connexion mobile limitée.',
  },
  {
    question: 'Mes données sont-elles en sécurité ?',
    answer: 'Absolument. Nous utilisons des protocoles de sécurité de niveau bancaire pour protéger vos informations. Vos données sont chiffrées et sauvegardées automatiquement sur plusieurs serveurs sécurisés.',
  },
  {
    question: 'Puis-je accepter les paiements par Mobile Money ?',
    answer: 'Oui, DriveHub est conçu pour le marché africain. Vous pouvez intégrer vos comptes Orange Money, MTN Mobile Money ou Wave pour recevoir les paiements de vos élèves directement sur la plateforme.',
  },
  {
    question: 'Combien de temps faut-il pour configurer mon auto-école ?',
    answer: 'L\'inscription prend moins de 2 minutes. Vous pouvez ensuite importer votre liste d\'élèves et configurer vos moniteurs immédiatement. Notre équipe support est également disponible pour vous accompagner gratuitement.',
  },
  {
    question: 'Puis-je changer de forfait à tout moment ?',
    answer: 'Oui, vous pouvez passer d\'un forfait à un autre (upgrade ou downgrade) à tout moment depuis votre tableau de bord. La différence sera calculée au prorata.',
  },
];

export const FAQ = () => {
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  return (
    <section id="faq" className="py-24 bg-white relative">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-primary font-bold tracking-wider uppercase text-sm mb-3">FAQ</h2>
          <h3 className="text-3xl md:text-4xl font-bold mb-6">Questions Fréquentes</h3>
          <p className="text-muted-foreground text-lg">
            Tout ce que vous devez savoir sur DriveHub et comment il peut transformer votre établissement.
          </p>
        </div>

        <div className="max-w-3xl mx-auto space-y-4">
          {faqs.map((faq, index) => (
            <div
              key={index}
              className="border border-border rounded-2xl overflow-hidden bg-slate-50/50 hover:bg-slate-50 transition-colors"
            >
              <button
                className="w-full px-6 py-5 flex items-center justify-between text-left"
                onClick={() => setActiveIndex(activeIndex === index ? null : index)}
              >
                <span className="font-bold text-lg">{faq.question}</span>
                {activeIndex === index ? <ChevronUp className="text-primary" /> : <ChevronDown className="text-muted-foreground" />}
              </button>
              
              <AnimatePresence>
                {activeIndex === index && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                    transition={{ duration: 0.3 }}
                  >
                    <div className="px-6 pb-6 text-muted-foreground leading-relaxed border-t border-border/50 pt-4">
                      {faq.answer}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))}
        </div>
        
        <div className="mt-16 bg-primary/5 rounded-3xl p-8 md:p-12 flex flex-col md:flex-row items-center justify-between gap-8">
           <div>
              <h4 className="text-2xl font-bold mb-2">Vous avez encore des questions ?</h4>
              <p className="text-muted-foreground">Notre équipe est là pour vous aider par WhatsApp ou Email.</p>
           </div>
           <div className="flex gap-4">
              <Button>Nous contacter</Button>
              <Button variant="outline">WhatsApp Support</Button>
           </div>
        </div>
      </div>
    </section>
  );
};
