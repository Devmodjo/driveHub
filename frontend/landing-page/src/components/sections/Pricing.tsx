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
    <section id="pricing" className="py-32 bg-white dark:bg-black border-y border-black/5 dark:border-white/5 relative overflow-hidden">
      <div className="container mx-auto px-6 relative z-10">
        <div className="text-center max-w-3xl mx-auto mb-24">
          <motion.h2 
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block"
          >
            {dict.Pricing.badge}
          </motion.h2>
          <motion.h3 
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="font-display text-4xl md:text-6xl font-black text-black dark:text-white mb-8 tracking-tight leading-[1.1]"
          >
            {dict.Pricing.title}
          </motion.h3>
          <motion.p 
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-xl md:text-2xl text-black/50 dark:text-white/50 font-light leading-relaxed"
          >
            {dict.Pricing.description}
          </motion.p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 max-w-7xl mx-auto items-stretch">
          {plans.map((plan, index) => (
            <motion.div
              key={plan.name}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1, duration: 0.6 }}
              whileHover={{ y: -8, transition: { duration: 0.3 } }}
              className={`p-1 bg-gradient-to-b from-black/[0.03] to-transparent dark:from-white/[0.03] dark:to-transparent rounded-[40px] border border-black/5 dark:border-white/5 flex flex-col relative h-full ${
                plan.popular ? 'ring-2 ring-[#0070f3]/40' : ''
              }`}
            >
              <div className="bg-white dark:bg-[#050505] p-12 rounded-[36px] h-full flex flex-col items-start">
                {plan.popular && (
                  <div className="absolute -top-5 left-1/2 -translate-x-1/2 px-8 py-2.5 rounded-full bg-[#0070f3] text-white text-[10px] font-black uppercase tracking-[0.2em] shadow-2xl shadow-[#0070f3]/40">
                    {dict.Pricing.popular}
                  </div>
                )}

                <div className="mb-10 w-full">
                  <h4 className="font-display text-3xl font-black text-black dark:text-white mb-4 tracking-tight">{plan.name}</h4>
                  <p className="text-black/50 dark:text-white/50 text-base mb-10 leading-relaxed font-light">{plan.description}</p>
                  <div className="flex items-baseline gap-2">
                    <span className="font-display text-6xl font-black text-black dark:text-white tracking-tighter">{plan.price}</span>
                    <span className="text-black/30 dark:text-white/30 font-black text-[10px] uppercase tracking-widest">{plan.currency}{plan.period}</span>
                  </div>
                </div>

                <div className="h-px w-full bg-black/5 dark:bg-white/5 mb-10" />

                <ul className="space-y-6 mb-16 flex-grow w-full">
                  {plan.features.map((feature, i) => (
                    <li key={i} className="flex items-start gap-4 text-base text-black/60 dark:text-white/60 font-light group/item">
                      <Check size={22} strokeWidth={3} className="text-[#0070f3] shrink-0 mt-0.5" />
                      <span>{feature}</span>
                    </li>
                  ))}
                </ul>

                <motion.button 
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className={`w-full py-5 rounded-2xl font-black transition-all text-[10px] uppercase tracking-[0.25em] flex items-center justify-center ${
                  plan.popular 
                  ? 'bg-[#0070f3] text-white shadow-xl shadow-[#0070f3]/20 hover:shadow-[#0070f3]/40' 
                  : 'bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 text-black dark:text-white hover:bg-black/10 dark:hover:bg-white/10'
                }`}>
                  {plan.cta}
                </motion.button>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};
