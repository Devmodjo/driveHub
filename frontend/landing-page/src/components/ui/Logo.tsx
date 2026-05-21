'use client';

import React from 'react';

interface LogoProps {
  variant?: 'light' | 'dark' | 'auto';
  className?: string;
  width?: number | string;
  height?: number | string;
}

export const Logo = ({ variant = 'auto', className = '', width = "100%", height = "100%" }: LogoProps) => {
  const blueColor = "#0070f3";
  
  return (
    <div className={className} style={{ width, height, position: 'relative' }}>
      {/* Light Variant Logo (shown when variant is 'light' or when 'auto' and parent is in light mode) */}
      <svg
        viewBox="0 0 250 60"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={`${variant === 'dark' ? 'hidden' : variant === 'light' ? 'block' : 'block dark:hidden'} w-full h-full`}
      >
        <g>
          {/* Speed Lines */}
          <rect x="5" y="18" width="18" height="4" rx="2" fill={blueColor} />
          <rect x="12" y="28" width="14" height="4" rx="2" fill={blueColor} />
          <rect x="8" y="38" width="16" height="4" rx="2" fill={blueColor} />
          
          {/* Drive Text */}
          <text 
            x="35" 
            y="45" 
            fill={blueColor} 
            style={{ font: 'italic 800 42px var(--font-jakarta)', letterSpacing: '-1.5px' }}
          >
            Drive
          </text>
          
          {/* Connection Line */}
          <path d="M142 38 L170 38" stroke={blueColor} strokeWidth="4" strokeLinecap="round" />
          <circle cx="174" cy="38" r="5" stroke="black" strokeWidth="2.5" fill="none" />
          
          {/* Hub Text with Outline */}
          <text 
            x="182" 
            y="45" 
            stroke="black" 
            strokeWidth="2"
            fill="none"
            style={{ font: 'italic 800 42px var(--font-jakarta)', letterSpacing: '-1.5px' }}
          >
            Hub
          </text>
        </g>
      </svg>

      {/* Dark Variant Logo (shown when variant is 'dark' or when 'auto' and parent is in dark mode) */}
      <svg
        viewBox="0 0 250 60"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={`${variant === 'light' ? 'hidden' : variant === 'dark' ? 'block' : 'hidden dark:block'} w-full h-full`}
      >
        <g>
          {/* Drive Text */}
          <text 
            x="5" 
            y="45" 
            fill={blueColor} 
            style={{ font: 'italic 800 42px var(--font-jakarta)', letterSpacing: '-1.5px' }}
          >
            Drive
          </text>
          
          {/* Connection Line */}
          <path d="M112 38 L140 38" stroke={blueColor} strokeWidth="4" strokeLinecap="round" />
          <circle cx="144" cy="38" r="5" stroke="white" strokeWidth="2.5" fill="none" />
          
          {/* Hub Text with Outline */}
          <text 
            x="152" 
            y="45" 
            stroke="white" 
            strokeWidth="2"
            fill="none"
            style={{ font: 'italic 800 42px var(--font-jakarta)', letterSpacing: '-1.5px' }}
          >
            Hub
          </text>
        </g>
      </svg>
    </div>
  );
};
