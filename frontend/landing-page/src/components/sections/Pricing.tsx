'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/Button';
import { Check } from 'lucide-react';

const plans = [
  {
    name: 'Startup',
    price: '25.000',
    currency: 'FCFA',
    period: '/mois',
    description: 'Parfait pour les petites auto-écoles qui débutent.',
    features: [
      'Jusqu\'à 50 élèves',
      'Gestion des moniteurs',
      'Planning de base',
      'Support par email',
    ],
    cta: 'Commencer',
    popular: false,
  },
  {
    name: 'Business',
    price: '50.000',
    currency: 'FCFA',
    period: '/mois',
    description: 'La solution complète pour une gestion optimale.',
    features: [
      'Élèves illimités',
      'Gestion de flotte (véhicules)',
      'Paiements Mobile Money',
      'SMS de rappel illimités',
      'Statistiques avancées',
      'Support prioritaire 24/7',
    ],
    cta: 'Essayer gratuitement',
    popular: true,
  },
  {
    name: 'Enterprise',
    price: 'Sur devis',
    currency: '',
    period: '',
    description: 'Pour les réseaux d\'auto-écoles multi-agences.',
    features: [
      'Multi-agences centralisées',
      'API personnalisée',
      'Formation sur site',
      'Gestionnaire de compte dédié',
      'Marque blanche',
    ],
    cta: 'Nous contacter',
    popular: false,
  },
];

export const Pricing = () => {
  return (
    <section id="pricing" className="py-24 bg-slate-50 relative">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-primary font-bold tracking-wider uppercase text-sm mb-3">Tarifs</h2>
          <h3 className="text-3xl md:text-4xl font-bold mb-6">Un investissement rentable pour votre croissance</h3>
          <p className="text-muted-foreground text-lg">
            Choisissez le forfait qui correspond à la taille de votre établissement. Pas de frais cachés.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {plans.map((plan, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              className={`relative bg-white rounded-3xl p-8 border ${
                plan.popular ? 'border-primary shadow-2xl shadow-primary/10' : 'border-border'
              }`}
            >
              {plan.popular && (
                <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-primary text-white text-xs font-bold uppercase tracking-widest px-4 py-1.5 rounded-full">
                  Plus populaire
                </div>
              )}
              
              <div className="mb-8">
                <h4 className="text-xl font-bold mb-2">{plan.name}</h4>
                <div className="flex items-baseline gap-1">
                  <span className="text-4xl font-extrabold">{plan.price}</span>
                  <span className="text-muted-foreground font-medium">{plan.currency}{plan.period}</span>
                </div>
                <p className="text-sm text-muted-foreground mt-4">{plan.description}</p>
              </div>

              <div className="space-y-4 mb-8">
                {plan.features.map((feature, i) => (
                  <div key={i} className="flex items-start gap-3">
                    <div className="mt-1 bg-primary/10 rounded-full p-0.5">
                       <Check size={16} className="text-primary" />
                    </div>
                    <span className="text-sm text-foreground/80">{feature}</span>
                  </div>
                ))}
              </div>

              <Button
                variant={plan.popular ? 'primary' : 'outline'}
                className="w-full"
                size="lg"
              >
                {plan.cta}
              </Button>
            </motion.div>
          ))}
        </div>
        
        <p className="text-center text-muted-foreground mt-12 text-sm">
          Besoin d'une offre sur mesure ? <a href="#" className="text-primary font-semibold underline">Parlons-en.</a>
        </p>
      </div>
    </section>
  );
};
