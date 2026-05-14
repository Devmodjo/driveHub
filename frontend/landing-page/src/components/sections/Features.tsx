'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { 
  Users, 
  Calendar, 
  CreditCard, 
  BookOpen, 
  Bell,
  LineChart
} from 'lucide-react';

const features = [
  {
    title: 'Dossiers Étudiants',
    description: 'Inscription en ligne, documents numérisés (CNI, certificats médicaux) et suivi complet de chaque élève en un clic.',
    icon: Users,
  },
  {
    title: 'Planning des Cours',
    description: 'Organisez vos séances théoriques et pratiques, assignez vos moniteurs et gérez les créneaux sans prise de tête.',
    icon: Calendar,
  },
  {
    title: 'Paiements Mobile Money',
    description: 'Collectez vos frais en Orange Money ou MTN MoMo, suivez les tranches et identifiez les impayés instantanément.',
    icon: CreditCard,
  },
  {
    title: 'Code de la Route',
    description: 'Vos élèves révisent le code camerounais depuis leur téléphone — tests interactifs, corrections et scores.',
    icon: BookOpen,
  },
  {
    title: 'Rappels Automatiques',
    description: 'SMS et WhatsApp pour les cours du lendemain, les paiements en retard et les dates d\'examen. Zéro oubli.',
    icon: Bell,
  },
  {
    title: 'Suivi de Progression',
    description: 'Visualisez l\'avancement de chaque élève — heures effectuées, résultats aux tests, prêt pour l\'examen ou pas.',
    icon: LineChart,
  },
];

export const Features = () => {
  return (
    <section id="features" className="py-24 bg-white dark:bg-black border-y border-black/5 dark:border-white/5">
      <div className="container mx-auto px-6">
        <div className="max-w-3xl mb-16">
          <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">L'Écosystème DriveHub</h2>
          <h3 className="font-display text-4xl md:text-5xl font-black text-black dark:text-white mb-6">Tout pour piloter votre réussite.</h3>
          <p className="text-xl text-black/60 dark:text-white/60 font-light">
            Une suite complète d'outils professionnels pensés pour répondre aux exigences des auto-écoles camerounaises, unifiée sur une seule plateforme bilingue.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="group bg-white dark:bg-black p-8 rounded-3xl border border-black/5 dark:border-white/5 hover:border-[#0070f3]/50 dark:hover:border-[#0070f3]/50 transition-all duration-300 relative overflow-hidden"
            >
              {/* Subtle hover glow effect */}
              <div className="absolute inset-0 bg-[#0070f3]/0 group-hover:bg-[#0070f3]/5 transition-colors duration-300 pointer-events-none" />
              
              <div className="w-14 h-14 rounded-2xl bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 flex items-center justify-center mb-8 text-black dark:text-white transition-transform group-hover:scale-110 group-hover:bg-[#0070f3] group-hover:text-white group-hover:border-[#0070f3]/50">
                <feature.icon size={28} strokeWidth={1.5} />
              </div>
              <h4 className="text-2xl font-bold text-black dark:text-white mb-4">{feature.title}</h4>
              <p className="text-black/60 dark:text-white/60 leading-relaxed font-light">
                {feature.description}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};
