export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  exactMatch?: boolean;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  badge?: {
    title?: string;
    type?: string;
  };
  children?: NavigationItem[];
}

export const NavigationItems: NavigationItem[] = [
  {
    id: 'navigation',
    title: 'Navigation',
    type: 'group',
    icon: 'icon-group',
    children: [
      {
        id: 'dashboard',
        title: 'Dashboard',
        type: 'item',
        url: '/dashboard/analytics',
        icon: 'feather icon-home'
      },
      {
        id: 'comments',
        title: 'Commentaires',
        type: 'item',
        url: '/dashboard/comments',
        icon: 'feather icon-message-square'
      },
      {
        id: 'consultations',
        title: 'Consultation',
        type: 'item',
        url: '/dashboard/consultations',
        icon: 'feather icon-shopping-cart'
      },
      {
        "id": "diet-group",
        "title": "Menu",
        "type": "collapse",
        "icon": "feather icon-book",
        "children": [
          {
            "id": "diet-list",
            "title": "Liste des Menu",
            "type": "item",
            "url": "/dashboard/diet",
            "icon": "feather icon-check-square"
          },
          {
            "id": "diet-categories",
            "title": "Catégories de menu",
            "type": "item",
            "url": "/dashboard/diet-categories",
            "icon": "feather icon-grid"
          }
        ]
      },
      {
        id: 'blogs-group',
        title: 'Blogs',
        type: 'collapse',
        icon: 'feather icon-file-text',
        children: [
          {
            id: 'blog-list',
            title: 'Liste des blogs',
            type: 'item',
            url: '/dashboard/blogs',
            icon: 'feather icon-list'
          },
          {
            id: 'blog-categories',
            title: 'Catégories de blog',
            type: 'item',
            url: '/dashboard/blog-categories',
            icon: 'feather icon-folder'
          }
        ]
      },
      {
        id: 'faqs-group',
        title: 'FAQs',
        type: 'collapse',
        icon: 'feather icon-help-circle',
        children: [
          {
            id: 'faq-category-list',
            title: 'Liste des catégories',
            type: 'item',
            url: '/dashboard/faq-categories',
            icon: 'feather icon-layers'
          },
          {
            id: 'question-aswser',
            title: 'Questions',
            type: 'item',
            url: '/dashboard/faqs',
            icon: 'feather icon-file-text'
          }
        ]
      },
      {
        id: 'services',
        title: 'Services',
        type: 'item',
        url: '/dashboard/services',
        icon: 'feather icon-server'
      },
      {
        id: 'news-letter',
        title: 'News Letter',
        type: 'item',
        url: '/dashboard/news-letter',
        icon: 'feather icon-mail'
      },
      {
        id: 'bookings',
        title: 'Rendez-vous',
        type: 'item',
        url: '/dashboard/bookings',
        icon: 'feather icon-shopping-cart'
      },
      {
        id: 'transactions',
        title: 'Transactions',
        type: 'item',
        url: '/dashboard/transactions',
        icon: 'feather icon-server'
      },
      {
        id: 'order',
        title: 'Commandes',
        type: 'item',
        url: '/dashboard/order',
        icon: 'feather icon-shopping-cart'
      },
      {
        id: 'settings',
        title: 'Paramètres',
        type: 'collapse',
        icon: 'feather icon-settings',
        children: [
          {
            id: 'pictures',
            title: 'Images',
            type: 'item',
            url: '/dashboard/pictures',
            icon: 'feather icon-image'
          },
          {
            id: 'caloricity',
            title: "Caloricités",
            type: 'item',
            url: '/dashboard/caloricity',
            icon: 'feather icon-bar-chart'  // Icône ajoutée
          }
        ]
      }
    ]
  },
  // {
  //   id: 'ui-component',
  //   title: 'Ui Component',
  //   type: 'group',
  //   icon: 'icon-group',
  //   children: [
  //     {
  //       id: 'basic',
  //       title: 'Component',
  //       type: 'collapse',
  //       icon: 'feather icon-box',
  //       children: [
  //         {
  //           id: 'button',
  //           title: 'Button',
  //           type: 'item',
  //           url: '/component/button'
  //         },
  //         {
  //           id: 'badges',
  //           title: 'Badges',
  //           type: 'item',
  //           url: '/component/badges'
  //         },
  //         {
  //           id: 'breadcrumb-pagination',
  //           title: 'Breadcrumb & Pagination',
  //           type: 'item',
  //           url: '/component/breadcrumb-paging'
  //         },
  //         {
  //           id: 'collapse',
  //           title: 'Collapse',
  //           type: 'item',
  //           url: '/component/collapse'
  //         },
  //         {
  //           id: 'tabs-pills',
  //           title: 'Tabs & Pills',
  //           type: 'item',
  //           url: '/component/tabs-pills'
  //         },
  //         {
  //           id: 'typography',
  //           title: 'Typography',
  //           type: 'item',
  //           url: '/component/typography'
  //         }
  //       ]
  //     }
  //   ]
  // },
  // {
  //   id: 'Authentication',
  //   title: 'Authentication',
  //   type: 'group',
  //   icon: 'icon-group',
  //   children: [
  //     {
  //       id: 'signup',
  //       title: 'Sign up',
  //       type: 'item',
  //       url: '/register',
  //       icon: 'feather icon-at-sign',
  //       target: true,
  //       breadcrumbs: false
  //     },
  //     {
  //       id: 'signin',
  //       title: 'Sign in',
  //       type: 'item',
  //       url: '/login',
  //       icon: 'feather icon-log-in',
  //       target: true,
  //       breadcrumbs: false
  //     }
  //   ]
  // },
  // {
  //   id: 'chart',
  //   title: 'Chart',
  //   type: 'group',
  //   icon: 'icon-group',
  //   children: [
  //     {
  //       id: 'apexchart',
  //       title: 'ApexChart',
  //       type: 'item',
  //       url: '/chart',
  //       classes: 'nav-item',
  //       icon: 'feather icon-pie-chart'
  //     }
  //   ]
  // },
  // {
  //   id: 'forms & tables',
  //   title: 'Forms & Tables',
  //   type: 'group',
  //   icon: 'icon-group',
  //   children: [
  //     {
  //       id: 'forms',
  //       title: 'Basic Forms',
  //       type: 'item',
  //       url: '/forms',
  //       classes: 'nav-item',
  //       icon: 'feather icon-file-text'
  //     },
  //     {
  //       id: 'tables',
  //       title: 'Tables',
  //       type: 'item',
  //       url: '/tables',
  //       classes: 'nav-item',
  //       icon: 'feather icon-server'
  //     }
  //   ]
  // },
  // {
  //   id: 'other',
  //   title: 'Other',
  //   type: 'group',
  //   icon: 'icon-group',
  //   children: [
  //     {
  //       id: 'sample-page',
  //       title: 'Sample Page',
  //       type: 'item',
  //       url: '/sample-page',
  //       classes: 'nav-item',
  //       icon: 'feather icon-sidebar'
  //     },
  //     {
  //       id: 'menu-level',
  //       title: 'Menu Levels',
  //       type: 'collapse',
  //       icon: 'feather icon-menu',
  //       children: [
  //         {
  //           id: 'menu-level-2.1',
  //           title: 'Menu Level 2.1',
  //           type: 'item',
  //           url: 'javascript:',
  //           external: true
  //         },
  //         {
  //           id: 'menu-level-2.2',
  //           title: 'Menu Level 2.2',
  //           type: 'collapse',
  //           children: [
  //             {
  //               id: 'menu-level-2.2.1',
  //               title: 'Menu Level 2.2.1',
  //               type: 'item',
  //               url: 'javascript:',
  //               external: true
  //             },
  //             {
  //               id: 'menu-level-2.2.2',
  //               title: 'Menu Level 2.2.2',
  //               type: 'item',
  //               url: 'javascript:',
  //               external: true
  //             }
  //           ]
  //         }
  //       ]
  //     }
  //   ]
  // }
];
