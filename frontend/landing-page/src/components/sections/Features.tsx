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
import { useDictionary } from '@/components/DictionaryProvider';

export const Features = () => {
  const { dict } = useDictionary();
  
  const features = [
    {
      title: dict.Features.f1_title,
      description: dict.Features.f1_desc,
      icon: Users,
    },
    {
      title: dict.Features.f2_title,
      description: dict.Features.f2_desc,
      icon: Calendar,
    },
    {
      title: dict.Features.f3_title,
      description: dict.Features.f3_desc,
      icon: CreditCard,
    },
    {
      title: dict.Features.f4_title,
      description: dict.Features.f4_desc,
      icon: BookOpen,
    },
    {
      title: dict.Features.f5_title,
      description: dict.Features.f5_desc,
      icon: Bell,
    },
    {
      title: dict.Features.f6_title,
      description: dict.Features.f6_desc,
      icon: LineChart,
    },
  ];

  return (
    <section id="features" className="py-24 bg-white dark:bg-black border-y border-black/5 dark:border-white/5">
      <div className="container mx-auto px-6">
        <div className="max-w-3xl mb-16">
          <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">{dict.Features.badge}</h2>
          <h3 className="font-display text-4xl md:text-5xl font-black text-black dark:text-white mb-6">{dict.Features.title}</h3>
          <p className="text-xl text-black/60 dark:text-white/60 font-light">
            {dict.Features.description}
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
