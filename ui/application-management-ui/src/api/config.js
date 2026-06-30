export const API = {
  appManagement: import.meta.env.VITE_APP_MANAGEMENT_API_URL ?? 'http://129.146.21.16:8081/api/v1/application-management',
  pricing: import.meta.env.VITE_PRICING_API_URL ?? 'http://129.146.21.16:8082/api/v1/pricing-orchestration',
  offerAcceptance: import.meta.env.VITE_OFFER_ACCEPTANCE_API_URL ?? 'http://129.146.21.16:8085/api/v1/offer-acceptance',
  document: import.meta.env.VITE_DOCUMENT_API_URL ?? 'http://129.146.21.16:8084/api/v1/document',
  pricingOffersUiJs: import.meta.env.VITE_PRICING_OFFERS_UI_JS_URL ?? 'http://129.146.21.16:3001/pricing-offer-selector.iife.js',
  documentManagementUiJs: import.meta.env.VITE_DOCUMENT_MANAGEMENT_UI_JS_URL ?? 'http://129.146.21.16:3002/document-upload-manager.iife.js',
};
