'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { 
  Users, 
  Calendar, 
  Car, 
  CreditCard, 
  BarChart3, 
  ShieldCheck,
  Smartphone,
  Zap
} from 'lucide-react';

const features = [
  {
    title: 'Gestion des Élèves',
    description: 'Suivez le dossier complet de chaque élève, de l\'inscription à l\'obtention du permis.',
    icon: Users,
    color: 'bg-blue-500',
  },
  {
    title: 'Planning Intelligent',
    description: 'Gérez les cours et les examens avec un calendrier interactif partagé entre moniteurs.',
    icon: Calendar,
    color: 'bg-indigo-500',
  },
  {
    title: 'Suivi des Véhicules',
    description: 'Gardez un œil sur l\'entretien, le carburant et la disponibilité de votre flotte.',
    icon: Car,
    color: 'bg-sky-500',
  },
  {
    title: 'Paiements & Factures',
    description: 'Automatisez la facturation et acceptez les paiements (compatible Mobile Money).',
    icon: CreditCard,
    color: 'bg-blue-600',
  },
  {
    title: 'Analyses de Performance',
    description: 'Visualisez la rentabilité de votre auto-école avec des rapports détaillés.',
    icon: BarChart3,
    color: 'bg-indigo-600',
  },
  {
    title: 'Sécurité des Données',
    description: 'Vos données sont chiffrées et sauvegardées quotidiennement sur nos serveurs sécurisés.',
    icon: ShieldCheck,
    color: 'bg-sky-600',
  },
  {
    title: 'Accès Mobile',
    description: 'Gérez votre établissement depuis votre smartphone, où que vous soyez.',
    icon: Smartphone,
    color: 'bg-blue-700',
  },
  {
    title: 'Notifications Instantanées',
    description: 'Envoyez des rappels automatiques aux élèves par SMS ou WhatsApp.',
    icon: Zap,
    color: 'bg-indigo-700',
  },
];

export const Features = () => {
  return (
    <section id="features" className="py-24 bg-white relative overflow-hidden">
      <div className="container mx-auto px-6 relative z-10">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-primary font-bold tracking-wider uppercase text-sm mb-3">Fonctionnalités Hub</h2>
          <h3 className="text-3xl md:text-4xl font-bold mb-6">Tout ce dont vous avez besoin pour piloter votre activité</h3>
          <p className="text-muted-foreground text-lg">
            DriveHub centralise tous vos outils de gestion dans une interface unique, fluide et intuitive.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              className="p-8 rounded-2xl border border-border hover:border-primary/50 hover:shadow-xl hover:shadow-primary/5 transition-all group"
            >
              <div className={`w-14 h-14 ${feature.color} text-white rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform`}>
                <feature.icon size={28} />
              </div>
              <h4 className="text-xl font-bold mb-3">{feature.title}</h4>
              <p className="text-muted-foreground leading-relaxed">
                {feature.description}
              </p>
            </motion.div>
          ))}
        </div>
      </div>

      {/* Decorative background elements */}
      <div className="absolute top-0 left-0 w-full h-20 bg-gradient-to-b from-slate-50 to-transparent" />
      <div className="absolute bottom-0 left-0 w-full h-20 bg-gradient-to-t from-slate-50 to-transparent" />
    </section>
  );
};
