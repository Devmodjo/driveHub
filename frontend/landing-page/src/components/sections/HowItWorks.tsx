'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { UserPlus, Settings, CheckCircle } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const HowItWorks = () => {
  const { dict } = useDictionary();

  const steps = [
    {
      title: dict.HowItWorks.step1_title,
      description: dict.HowItWorks.step1_desc,
      icon: UserPlus,
    },
    {
      title: dict.HowItWorks.step2_title,
      description: dict.HowItWorks.step2_desc,
      icon: Settings,
    },
    {
      title: dict.HowItWorks.step3_title,
      description: dict.HowItWorks.step3_desc,
      icon: CheckCircle,
    },
  ];

  return (
    <section id="how-it-works" className="py-24 bg-white dark:bg-black border-y border-black/5 dark:border-white/5">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">{dict.HowItWorks.badge}</h2>
          <h3 className="font-display text-4xl md:text-5xl font-black text-black dark:text-white mb-6">{dict.HowItWorks.title}</h3>
          <p className="text-xl text-black/60 dark:text-white/60 font-light">
            {dict.HowItWorks.description}
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 max-w-5xl mx-auto">
          {steps.map((step, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="relative text-center group"
            >
              <div className="w-20 h-20 rounded-3xl bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 text-[#0070f3] flex items-center justify-center mx-auto mb-8 shadow-sm group-hover:bg-[#0070f3] group-hover:text-white group-hover:border-[#0070f3]/50 transition-all duration-300">
                <step.icon size={32} strokeWidth={1.5} />
              </div>
              
              <h4 className="text-2xl font-bold text-black dark:text-white mb-4">{step.title}</h4>
              <p className="text-black/60 dark:text-white/60 leading-relaxed font-light">
                {step.description}
              </p>

              {index < steps.length - 1 && (
                <div className="hidden lg:block absolute top-10 left-[70%] w-[60%] h-[1px] bg-black/10 dark:bg-white/10 border-0" />
              )}
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};
