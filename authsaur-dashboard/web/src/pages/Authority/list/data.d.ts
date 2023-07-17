export type Member = {
  avatar: string;
  name: string;
  id: string;
};

export type CardListItemDataType = {
  id: string;
  name: string;
  type: string;
  serviceId: string;
  avatar: string;
  logo: string;
  evaluationOrder: number;
  description: string;
  metadataLocation: string;
  signAssertions: string;
  star: number;
  like: number;
  message: number;
  members: Member[];
};
