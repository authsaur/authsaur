
import type { Request, Response } from 'express';

export default {
  'POST  /api/sync': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'PUT  /api/sync': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'DELETE  /api/sync/:id': (_: Request, res: Response) => {
    res.send({ data: { message: 'Ok' }, success: true });
  },
  'GET /api/sync/:id': (req: Request, res: Response) => {
    res.send({
      payAccount: '1ant-design@alipay.com',
      receiverAccount: '1test@example.com',
      receiverName: 'Alex1',
      amount: '500',
      serviceId: 'alipay',
      type: 'OAuth2',
    });
  },
};
