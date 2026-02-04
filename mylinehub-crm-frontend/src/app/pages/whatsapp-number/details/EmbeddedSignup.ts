export type EmbeddedSignupResult = {
  // your tenant
  organization: string;

  // Meta auth (required)
  code: string;

  // Meta embedded signup sessionInfo (required)
  waba_id: string;
  phone_number_id: string;
  business_id: string;

  // optional (Meta sometimes provides)
  state?: string;
};
