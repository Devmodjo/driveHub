'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Search } from 'lucide-react';

export const Hero = () => {
  return (
    <section className="relative min-h-[85vh] flex items-center pt-32 pb-20 bg-white dark:bg-black overflow-hidden selection:bg-[#0070f3]/30">
      {/* Background Subtle Gradient Glows for modern SaaS feel */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-full max-w-4xl h-[400px] bg-[#0070f3]/5 dark:bg-[#0070f3]/10 blur-[130px] rounded-full pointer-events-none" />

      <div className="container mx-auto px-6 relative z-10">
        <div className="max-w-4xl mx-auto text-center">
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="font-display text-5xl md:text-7xl font-black tracking-tighter text-black dark:text-white mb-8 leading-[1.1]"
          >
            Moins de paperasse. <span className="text-transparent bg-clip-text bg-linear-to-r from-[#0070f3] to-[#4096ff]">Plus de conduite.</span>
          </motion.h1>
          
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="text-lg md:text-2xl text-black/60 dark:text-white/60 mb-12 leading-relaxed max-w-3xl mx-auto font-light"
          >
            Inscriptions, plannings, paiements mobiles et suivi pédagogique : la plateforme définitive pour piloter votre établissement au Cameroun ou trouver l'école idéale.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="flex flex-col sm:flex-row gap-4 justify-center items-center"
          >
            <button className="w-full sm:w-auto bg-[#0070f3] text-white px-8 py-4 rounded-full font-bold text-lg hover:bg-[#0070f3]/90 transition-all flex items-center justify-center gap-3 shadow-lg shadow-[#0070f3]/25">
              Gérer mon auto-école <ArrowRight size={20} />
            </button>
            <button className="w-full sm:w-auto bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 text-black dark:text-white px-8 py-4 rounded-full font-medium text-lg hover:bg-black/10 dark:hover:bg-white/10 transition-all backdrop-blur-md flex items-center justify-center gap-3">
              Trouver une auto-école <Search size={20} />
            </button>
          </motion.div>
        </div>
      </div>
    </section>
  );
};
