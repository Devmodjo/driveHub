'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { motion } from 'framer-motion';
import { ArrowRight, Play, CheckCircle2 } from 'lucide-react';

export const Hero = () => {
  return (
    <section className="relative pt-32 pb-20 overflow-hidden">
      {/* Decorative Hub Background */}
      <div className="absolute inset-0 hub-grid opacity-40 -z-10" />
      <div className="absolute top-1/4 -right-20 w-96 h-96 bg-primary/10 rounded-full blur-3xl -z-10" />
      <div className="absolute bottom-1/4 -left-20 w-96 h-96 bg-secondary/10 rounded-full blur-3xl -z-10" />

      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-12">
          <motion.div 
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6 }}
            className="lg:w-1/2 text-center lg:text-left"
          >
            <div className="inline-flex items-center gap-2 bg-primary/10 text-primary px-4 py-1.5 rounded-full text-sm font-semibold mb-6">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
              </span>
              Solution n°1 pour les auto-écoles en Afrique
            </div>
            
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold text-foreground leading-tight mb-6">
              Propulsez votre <span className="text-primary">Auto-École</span> vers le futur du digital.
            </h1>
            
            <p className="text-lg text-muted-foreground mb-10 max-w-2xl mx-auto lg:mx-0">
              DriveHub est le hub centralisé pour gérer vos élèves, moniteurs, véhicules et paiements en un seul endroit. Simple, moderne et adapté à votre réalité locale.
            </p>

            <div className="flex flex-col sm:flex-row items-center gap-4 justify-center lg:justify-start">
              <Button size="lg" className="gap-2 w-full sm:w-auto">
                Démarrer maintenant <ArrowRight size={20} />
              </Button>
              <Button variant="outline" size="lg" className="gap-2 w-full sm:w-auto">
                <Play size={20} fill="currentColor" /> Voir la démo
              </Button>
            </div>

            <div className="mt-8 flex flex-wrap items-center justify-center lg:justify-start gap-6 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={18} className="text-primary" />
                Essai gratuit 14 jours
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 size={18} className="text-primary" />
                Sans carte bancaire
              </div>
            </div>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="lg:w-1/2 relative"
          >
            {/* Mockup Placeholder */}
            <div className="relative z-10 bg-white rounded-3xl shadow-2xl p-4 border border-border overflow-hidden">
               <div className="aspect-[16/10] bg-slate-100 rounded-2xl flex items-center justify-center overflow-hidden relative">
                  <div className="absolute inset-0 hub-grid opacity-20"></div>
                  {/* Visual representation of the dashboard */}
                  <div className="w-full h-full p-6 flex flex-col gap-4">
                     <div className="flex justify-between items-center mb-4">
                        <div className="h-8 w-32 bg-primary/20 rounded-md animate-pulse"></div>
                        <div className="h-8 w-8 bg-primary/20 rounded-full animate-pulse"></div>
                     </div>
                     <div className="grid grid-cols-3 gap-4">
                        <div className="h-24 bg-white border border-border rounded-xl p-3 flex flex-col justify-between">
                           <div className="h-2 w-12 bg-slate-200 rounded"></div>
                           <div className="h-4 w-8 bg-primary/40 rounded"></div>
                        </div>
                        <div className="h-24 bg-white border border-border rounded-xl p-3 flex flex-col justify-between">
                           <div className="h-2 w-12 bg-slate-200 rounded"></div>
                           <div className="h-4 w-8 bg-secondary/40 rounded"></div>
                        </div>
                        <div className="h-24 bg-white border border-border rounded-xl p-3 flex flex-col justify-between">
                           <div className="h-2 w-12 bg-slate-200 rounded"></div>
                           <div className="h-4 w-8 bg-accent/40 rounded"></div>
                        </div>
                     </div>
                     <div className="h-32 bg-white border border-border rounded-xl"></div>
                  </div>
               </div>
            </div>
            
            {/* Floating UI Elements */}
            <motion.div 
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
              className="absolute -top-6 -right-6 bg-white p-4 rounded-2xl shadow-xl border border-border z-20 hidden md:block"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-green-100 text-green-600 rounded-full flex items-center justify-center font-bold">
                  +12
                </div>
                <div>
                  <div className="text-xs text-muted-foreground">Nouveaux élèves</div>
                  <div className="font-bold text-sm">Ce mois-ci</div>
                </div>
              </div>
            </motion.div>

            <motion.div 
              animate={{ y: [0, 10, 0] }}
              transition={{ duration: 5, repeat: Infinity, ease: "easeInOut", delay: 1 }}
              className="absolute -bottom-6 -left-6 bg-white p-4 rounded-2xl shadow-xl border border-border z-20 hidden md:block"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-bold">
                   <CheckCircle2 size={20} />
                </div>
                <div>
                  <div className="text-xs text-muted-foreground">Planning</div>
                  <div className="font-bold text-sm">100% Optimisé</div>
                </div>
              </div>
            </motion.div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};
