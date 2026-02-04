import { NbMenuItem } from '@nebular/theme';

export var MENU_ITEMS: NbMenuItem[] = [

  {
    title: 'Whats-App Modules',
    group: true,
  },
  {
    title: 'Delivery Report',
    icon: 'message-square-outline',
    children: [
      {
        title: 'Dashboard',
        link: '/pages/whatsapp-report/report',
      },
    ],
  },
  {
    title: 'Facebook Project',
    icon: 'cube-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/whatsapp-project/details',
      }
    ],
  },
  {
    title: 'Installed Numbers',
    icon: 'phone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/whatsapp-number/details',
      },
    ],
  },
  {
    title: 'What\'sApp Chat',
    icon: 'message-circle-outline',
    children: [
      {
        title: 'Message Console',
        link: '/pages/whatsapp-chat/chat',
      },
    ],
  },
  {
    title: 'Media Storage',
    icon: 'briefcase-outline',
    children: [
      {
        title: 'All Files',
        link: '/pages/file-storage/all-files',
      },
    ],
  },
  {
    title: 'Campaign Modules',
    group: true,
  },
  {
    title: 'Campaign',
    icon: 'pantone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/campaign/all-campaigns',
      },
    ],
  },
  {
    title: 'Organization Module',
    group: true,
  },
  {
    title: 'Employee',
    icon: 'person-outline',
    children: [
      {
        title: 'Profile',
        link: '/pages/employee/profile',
      },
      {
        title: 'All Employees',
        link: '/pages/employee/all-employees',
      },
    ],
  },
  {
    title: 'Absenteeism',
    icon: 'close-square-outline',
    children: [
      {
        title: 'My Absenteeism',
        link: '/pages/absenteeism/my-absenteeism',
      },
      {
        title: 'All Absenteeism',
        link: '/pages/absenteeism/all-absenteeism',
      },
    ],
  },
  {
    title: 'Department',
    icon: 'smartphone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/department/all-departments',
      },
    ],
  },
  {
    title: 'Customer',
    icon: 'person-add-outline',
    children: [
      {
        title: 'Autodial Preview',
        link: '/pages/customer/preview-customers',
      },
      // {
      //   title: 'Schedule Preview',
      //   link: '/pages/customer/preview-schedule-customers',
      // },
      {
        title: 'All Customers',
        link: '/pages/customer/all-customers',
      },
    ],
  },
  {
    title: 'Product',
    icon: 'archive-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/product/all-products',
      },
    ],
  },
  {
    title: 'Purchase',
    icon: 'clipboard-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/purchase/all-purchases',
      },
    ],
  },
  {
    title: 'Supplier',
    icon: 'credit-card-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/supplier/all-suppliers',
      },
    ],
  },

  {
    title: 'Issue Tracking',
    group: true,
  },

  {
    title: 'Errors',
    icon: 'slash-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/error/all-errors',
      },
    ],
  },
  {
    title: 'Logs',
    icon: 'save-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/log/all-logs',
      },
    ],
  },
  {
    title: 'Auth',
    icon: 'lock-outline',
    children: [
      {
        title: 'Reset Password',
        link: '/pages/reset-password/reset',
      },      
    ],
  }
];

export var MENU_ITEMS_COPY: NbMenuItem[] = [

  {
    title: 'Whats-App',
    group: true,
  },
  {
    title: 'Delivery Report',
    icon: 'message-square-outline',
    children: [
      {
        title: 'Dashboard',
        link: '/pages/whatsapp-report/report',
      },
    ],
  },
  {
    title: 'Facebook Project',
    icon: 'cube-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/whatsapp-project/details',
      }
    ],
  },
  {
    title: 'Installed Numbers',
    icon: 'phone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/whatsapp-number/details',
      },
    ],
  },
  {
    title: 'What\'sApp Chat',
    icon: 'message-circle-outline',
    children: [
      {
        title: 'Message Console',
        link: '/pages/whatsapp-chat/chat',
      },
    ],
  },
  {
    title: 'Media Storage',
    icon: 'briefcase-outline',
    children: [
      {
        title: 'All-Media',
        link: '/pages/file-storage/all-files',
      },
    ],
  },
  {
    title: 'Calling Modules',
    group: true,
  },
  {
    title: 'Call Details',
    icon: 'phone-call-outline',
    children: [
      {
        title: 'Dashboard',
        link: '/pages/call-detail/call-dashboard',
      },
      {
        title: 'Search Terminal',
        link: '/pages/call-detail/all-calls',
      }
    ],
  },
  {
    title: 'Calling Cost',
    icon: 'trending-up-outline',
    children: [
      {
        title: 'Track All',
        link: '/pages/calling-cost/all-costs',
      },
    ],
  },
  {
    title: 'Campaign',
    icon: 'pantone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/campaign/all-campaigns',
      },
    ],
  },
  {
    title: 'Conference',
    icon: 'people-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/conference/all-conferences',
      },
    ],
  },
  {
    title: 'IVR',
    icon: 'shake-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/ivr/all-ivrs',
      },
    ],
  },
  {
    title: 'Queue',
    icon: 'more-horizontal-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/queue/all-queues',
      },
    ],
  },


  {
    title: 'Organization Module',
    group: true,
  },
  {
    title: 'Employee',
    icon: 'person-outline',
    children: [
      {
        title: 'Profile',
        link: '/pages/employee/profile',
      },
      {
        title: 'Call History',
        link: '/pages/employee/employee-call-history',
      },
      {
        title: 'All Employees',
        link: '/pages/employee/all-employees',
      },
      {
        title: 'Monitor Employees',
        link: '/pages/employee/monitor-employees',
      },
    ],
  },
  {
    title: 'File Storage',
    icon: 'briefcase-outline',
    children: [
      {
        title: 'All Files',
        link: '/pages/file-storage/all-files',
      },
    ],
  },
  {
    title: 'Absenteeism',
    icon: 'close-square-outline',
    children: [
      {
        title: 'My Absenteeism',
        link: '/pages/absenteeism/my-absenteeism',
      },
      {
        title: 'All Absenteeism',
        link: '/pages/absenteeism/all-absenteeism',
      },
    ],
  },
  {
    title: 'Department',
    icon: 'smartphone-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/department/all-departments',
      },
    ],
  },
  {
    title: 'Customer',
    icon: 'person-add-outline',
    children: [
      {
        title: 'Autodial Preview',
        link: '/pages/customer/preview-customers',
      },
      {
        title: 'All Customers',
        link: '/pages/customer/all-customers',
      },
    ],
  },
  {
    title: 'Product',
    icon: 'archive-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/product/all-products',
      },
    ],
  },
  {
    title: 'Purchase',
    icon: 'clipboard-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/purchase/all-purchases',
      },
    ],
  },
  {
    title: 'Supplier',
    icon: 'credit-card-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/supplier/all-suppliers',
      },
    ],
  },

  {
    title: 'Issue Tracking',
    group: true,
  },

  {
    title: 'Errors',
    icon: 'slash-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/error/all-errors',
      },
    ],
  },
  {
    title: 'Logs',
    icon: 'save-outline',
    children: [
      {
        title: 'Details',
        link: '/pages/log/all-logs',
      },
    ],
  },

  {
    title: 'Settings',
    group: true,
  },

  {
    title: 'AMI CONNECTIONS',
    icon: 'globe-2-outline',
    children: [
      {
        title: 'Registries',
        link: '/pages/ami-connection/registries',
      },
    ],
  },
  {
    title: 'SIP PPROVIDERS',
    icon: 'funnel-outline',
    children: [
      {
        title: 'Registries',
        link: '/pages/sip-provider/registries',
      },
    ],
  },
  {
    title: 'SSH CONNECTIONS',
    icon: 'globe-outline',
    children: [
      {
        title: 'Registries',
        link: '/pages/ssh-connection/registries',
      },
    ],
  },
  {
    title: 'Auth',
    icon: 'lock-outline',
    children: [
      {
        title: 'Reset Password',
        link: '/pages/reset-password/reset',
      },      
    ],
  }
];
