import { Routes } from '@angular/router';

/**
 * Rotas públicas do portal. URLs legíveis, minúsculas e sem acentos,
 * conforme a seção 20 da especificação.
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./public/home/home').then((m) => m.Home),
  },
  {
    path: 'imoveis',
    loadComponent: () =>
      import('./public/property-search/property-search').then((m) => m.PropertySearch),
  },
  {
    path: 'imoveis/:slug',
    loadComponent: () =>
      import('./public/property-detail/property-detail').then((m) => m.PropertyDetailPage),
  },
  {
    // Ambiente administrativo: casca própria, sem o cabeçalho e o rodapé públicos.
    path: 'admin',
    loadComponent: () => import('./admin/admin-shell').then((m) => m.AdminShell),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./admin/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'imoveis',
        pathMatch: 'full',
        loadComponent: () =>
          import('./admin/property-list/property-list').then((m) => m.PropertyList),
      },
      {
        path: 'imoveis/novo',
        loadComponent: () =>
          import('./admin/property-form/property-form').then((m) => m.PropertyForm),
      },
      {
        path: 'imoveis/:id/editar',
        loadComponent: () =>
          import('./admin/property-form/property-form').then((m) => m.PropertyForm),
      },
      {
        path: 'parceiros',
        pathMatch: 'full',
        loadComponent: () =>
          import('./admin/partner-list/partner-list').then((m) => m.PartnerList),
      },
      {
        path: 'parceiros/novo',
        loadComponent: () =>
          import('./admin/partner-form/partner-form').then((m) => m.PartnerForm),
      },
      {
        path: 'parceiros/:id/editar',
        loadComponent: () =>
          import('./admin/partner-form/partner-form').then((m) => m.PartnerForm),
      },
      {
        path: 'parceiros/:id',
        loadComponent: () =>
          import('./admin/partner-detail/partner-detail').then((m) => m.PartnerDetailPage),
      },
      {
        path: 'leads',
        pathMatch: 'full',
        loadComponent: () => import('./admin/lead-list/lead-list').then((m) => m.LeadList),
      },
      {
        path: 'leads/novo',
        loadComponent: () => import('./admin/lead-form/lead-form').then((m) => m.LeadForm),
      },
      {
        path: 'leads/:id/editar',
        loadComponent: () => import('./admin/lead-form/lead-form').then((m) => m.LeadForm),
      },
      {
        path: 'leads/:id',
        loadComponent: () => import('./admin/lead-detail/lead-detail').then((m) => m.LeadDetailPage),
      },
      {
        path: 'proprietarios',
        pathMatch: 'full',
        loadComponent: () => import('./admin/owner-list/owner-list').then((m) => m.OwnerList),
      },
      {
        path: 'proprietarios/novo',
        loadComponent: () => import('./admin/owner-form/owner-form').then((m) => m.OwnerForm),
      },
      {
        path: 'proprietarios/:id/editar',
        loadComponent: () => import('./admin/owner-form/owner-form').then((m) => m.OwnerForm),
      },
      {
        path: 'proprietarios/:id',
        loadComponent: () => import('./admin/owner-detail/owner-detail').then((m) => m.OwnerDetailPage),
      },
      {
        path: 'corretores',
        pathMatch: 'full',
        loadComponent: () => import('./admin/broker-list/broker-list').then((m) => m.BrokerList),
      },
      {
        path: 'corretores/novo',
        loadComponent: () => import('./admin/broker-form/broker-form').then((m) => m.BrokerForm),
      },
      {
        path: 'corretores/:id/editar',
        loadComponent: () => import('./admin/broker-form/broker-form').then((m) => m.BrokerForm),
      },
      {
        path: 'corretores/:id',
        loadComponent: () => import('./admin/broker-detail/broker-detail').then((m) => m.BrokerDetailPage),
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./public/not-found/not-found').then((m) => m.NotFound),
  },
];
