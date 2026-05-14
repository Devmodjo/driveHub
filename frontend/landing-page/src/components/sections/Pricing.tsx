'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { Check } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const Pricing = () => {
  const { dict } = useDictionary();

  const plans = [
    {
      name: dict.Pricing.plan1_name,
      price: '25.000',
      currency: 'FCFA',
      period: dict.Pricing.period,
      description: dict.Pricing.plan1_desc,
      features: [
        dict.Pricing.plan1_f1,
        dict.Pricing.plan1_f2,
        dict.Pricing.plan1_f3,
        dict.Pricing.plan1_f4,
      ],
      cta: dict.Pricing.plan1_cta,
      popular: false,
    },
    {
      name: dict.Pricing.plan2_name,
      price: '50.000',
      currency: 'FCFA',
      period: dict.Pricing.period,
      description: dict.Pricing.plan2_desc,
      features: [
        dict.Pricing.plan2_f1,
        dict.Pricing.plan2_f2,
        dict.Pricing.plan2_f3,
        dict.Pricing.plan2_f4,
        dict.Pricing.plan2_f5,
        dict.Pricing.plan2_f6,
      ],
      cta: dict.Pricing.plan2_cta,
      popular: true,
    },
    {
      name: dict.Pricing.plan3_name,
      price: dict.Pricing.plan3_price,
      currency: '',
      period: '',
      description: dict.Pricing.plan3_desc,
      features: [
        dict.Pricing.plan3_f1,
        dict.Pricing.plan3_f2,
        dict.Pricing.plan3_f3,
        dict.Pricing.plan3_f4,
      ],
      cta: dict.Pricing.plan3_cta,
      popular: false,
    },
  ];

  return (
    <section id="pricing" className="py-24 bg-white dark:bg-black border-y border-black/5 dark:border-white/5">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-20">
          <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">{dict.Pricing.badge}</h2>
          <h3 className="font-display text-4xl md:text-5xl font-black text-black dark:text-white mb-6">{dict.Pricing.title}</h3>
          <p className="text-xl text-black/60 dark:text-white/60 font-light">
            {dict.Pricing.description}
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {plans.map((plan, index) => (
            <motion.div
              key={plan.name}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              className={`relative p-8 rounded-3xl border flex flex-col group transition-all duration-300 ${
                plan.popular 
                ? 'border-[#0070f3] bg-white dark:bg-black shadow-[0_0_40px_rgba(0,112,243,0.1)] hover:shadow-[0_0_60px_rgba(0,112,243,0.15)] z-10 scale-100 lg:scale-105' 
                : 'border-black/5 dark:border-white/5 bg-black/5 dark:bg-white/5 hover:border-[#0070f3]/50'
              }`}
            >
              {plan.popular && (
                <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1.5 rounded-full bg-[#0070f3] text-white text-[10px] font-black uppercase tracking-widest shadow-lg shadow-[#0070f3]/25">
                  {dict.Pricing.popular}
                </div>
              )}

              <div className="mb-8">
                <h4 className="font-display text-2xl font-bold text-black dark:text-white mb-2">{plan.name}</h4>
                <p className="text-black/60 dark:text-white/60 text-sm mb-6 leading-relaxed font-light">{plan.description}</p>
                <div className="flex items-baseline gap-1">
                  <span className="font-display text-5xl font-black text-black dark:text-white tracking-tighter">{plan.price}</span>
                  <span className="text-black/50 dark:text-white/50 font-bold text-sm">{plan.currency}{plan.period}</span>
                </div>
              </div>

              <ul className="space-y-4 mb-10 flex-grow">
                {plan.features.map((feature, i) => (
                  <li key={i} className="flex items-start gap-3 text-sm text-black/80 dark:text-white/80 font-light">
                    <Check size={18} strokeWidth={2.5} className="text-[#0070f3] shrink-0 mt-0.5" />
                    <span>{feature}</span>
                  </li>
                ))}
              </ul>

              <button className={`w-full py-4 rounded-full font-bold transition-all text-sm uppercase tracking-wide flex items-center justify-center ${
                plan.popular 
                ? 'bg-[#0070f3] text-white hover:bg-[#0070f3]/90 shadow-lg shadow-[#0070f3]/25' 
                : 'bg-white dark:bg-black border border-black/10 dark:border-white/10 text-black dark:text-white hover:bg-black/5 dark:hover:bg-white/5'
              }`}>
                {plan.cta}
              </button>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};
