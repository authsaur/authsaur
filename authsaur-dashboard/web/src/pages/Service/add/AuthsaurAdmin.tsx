import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateAuthsaurAdmin } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'authsaur-admin' };

  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAYAAABccqhmAAAAAXNSR0IArs4c6QAAHDBJREFUeF7tXVuMXtdV3r8zjS1QocQ8VHnAqUqQmiZk4kTNTcrMiFbi8gCiRcR5iVHTxiERcUqhSDCZThwkClXropZccJXLQ20qUolW5aVFnrGE6kZJPFHdFNWtmvBQnhKMIpAnmXrQ+uff4zPn35e1r2efs78jRXb87+u31vr2Wmvvs89I4AECQKBaBEbVzhwTBwJAQIAAoARAoGIEQAAVCx9TBwIgAOgAEKgYARBAxcLH1IEACAA6AAQqRgAEULHwMXUgAAKADgCBihEAAVQsfEwdCIAAoANAoGIEQAAVCx9TBwIgAOgAEKgYARBAxcLH1IEACAA6AAQqRgAEULHwMXUgAAKADgCBihEAAVQsfEwdCIAAoANAoGIEQAAVCx9TBwIgAOgAEKgYARBAxcLH1IEACAA6AAQqRgAEULHwMXUgAAKADgCBihEAAVQsfEwdCIAAoANAoGIEQAAVCx9TBwIgAOgAEKgYARBAxcLH1IEACAA6AAQqRgAEULHwMXUgAAKADmwjMLtnZn7twsYKIKkHARBAPbKemikZvLgo5sVIzIlNMd8osLz21sanK4ammqmDAKoRtRAGg59GYSRW1tY3FiqCp8qpggAGLHYng1fjAE9gwPpBUwMBDEjA2wa/Nael4KnBCwiGsPQGQAClS8gyvtnLZ2SsHm7wir7W3tqAjvRcR0zDh3B7LNyJ8Ucx/Jm914qN18+q0EAY0GMdsQ0dBGBDqNDfJ+7+Sd/hkcHP7L1OXHYF/bll/P97+i9BAL6A9rQeCKCngpvdPXOytXVnnEnb4FWF/+ebv6tsA2FAT5WEMWwQAAOkEovMXj6zaRqXNHgqs/vqO1lTWD93Qlz44fHpsrvEAg4IsSDsXSEQQO9EtjVgXfy/59cOsA2+PXUtAQiBPEBP9cQ2bBCADaFCf09BADRVZRiA7cBCtSB8WCCAcAw7a0EVBoR4ADQRnReAPEBnYk7aMQggKbxpG9flAX7xd/7Fu2OEAd7Q9bIiCKCXYjPnAUIIQBsGIA/QY03RDx0E0GOxpsoD0HkA1aEghAE9VhbN0EEAPZdpzjyAgBfQc22ZHj4IoOciTZEHwKnAniuFw/BBAA5glVhUFwYkygMIsUvgjoAARSjtQBUIIECYJVRNlQcw7AaUMO0+j6GoQ1UggD6r0mTsmfMAA0Cs8ykUQwIggM51IXwAKfIAhu3A8AGjBUKgCBIAAQxAGXVhwM/f8tfjV319H4QBvsix6oEAWDChkBWBVHkA6hgkYIXftwAIwBc51JtGIEUeQPZC24I/e0N5WxBEwUBA+Yo1QgAGcijCRiBVHoA9ABTUIqC5aAUeAHQmHgKp8gDxRlhvSyCAemWfbeYp8wDZJjHQjkAAAxVsadNKmQcoba59Gg8IoE/S6vFYdReFhh4L7jEkRQwdBFCEGIY/COQBypQxCKBMuQxuVMgDlClSEECZchnkqJAHKE+sIIDyZDLYESEPUJ5oQQDlyWSwI0IeoDzRggDKk8lgR9RFHoCOCq+f2/qiUPsuQXoZib4/SNeV1/qAAGqVfEfzzpUHkIav+arw1OzpE2U1EgEIoCNDqLXbXHkAesmF3hZ0eWokARCAi4agbDACOfMAui8KmyZBYQHdVVDLAwKoRdKFzDNnHsDHCyCYaiIBEEAhhlHTMFR5gFRG5+MFkCxCbyzqizxBAH2RVI/HOVn1aQZLpmmkeC8AXoBZcUAAPTasPgxdl/RTjT3VquvrBdSQFAQB9MGKejrG2T0z8+KiOMkdfujnw3X9wAvQSwAEwNVOlHNGwGX1T518031U1DapVF6Jrd9cv4MAciFdWT+uq7+EJ0UegNo2fFPQKBkQQHeKi+8CdId9cM+uq7/sMKXB+XgBKccj57z+k2/swPuyX3hPMP7cBggTxYNLQbkAotw0ArrVn7b6ZvZeJzRXUY8bSmlwPl5AyvGEeCaJ9Q4EkBjgQTevW/2lMakMMVUCsA20qxeQKiSR43IdTybFAQFkAnpw3ehO+rUP+jRf1sll/K4rbuptQN/diQxKAwLIAPIgu9B9BCS1K+0CJnfVTb36gwDMUkMS0EWrCyhriv1LesGGkwtIvfpLcbW34W68eVbcePP1O6T55N8/k1u68AByIz6E/nQEkNPF5+Jo8gJyGT+NVeUFvPDjS2enXvzumrj3rod2TIt2CfZccw93qsZy2AWIAiMaIQRMe/+p3WlXCei8gFQvJenGpyKAj//J3eLjDx4cVyHjJxJoPjHDKRwEctUclDcioMsB9MUL6IKoVEYovYCb3ruwA+/YBAUCgEFHRUC3C0CddGFcpsk1vQAyrN1XHxjfBZD70XkBNI52/B87PAEBeEibXN21CxsrHlWrqNI3L4CE0nWSsm2IFAaokn+xSRQE4GiS23HuSKyIkVgGEUwD2CcvwFH8yYpztgRju/80GRCAo0inTrmBCJQI9skLcFSBJMU5BBAz+ScnAQJwEKdpZRO7xAK8gUtgwgtwUKxJUdPFJSlWf3gADjKyvd669tYGDi618Mx5AaiDKIstavICUqz+IAAHVTC+3joSK2vrGzv3axzaHmpReAHuklV5AalWfxAAUz621V+AALRIwgtgKtmkmMoLSLX6gwCYstEltBrVizg7zZxO9mI6/GJvaWWfWIIO6WzCxuvf29Fyyk+WIQloEaIx8SfrIgFoRBFeQAKmiNQkCMAApNX1BwGw1bAPrwmzJzOggiAAEwHsnjkpNsW8Td7YAbAhJETObwLaR4MSEgEQgEYX2Ks/EoAsa7JdE8ZqBIWiIwAC0BEAc/XHDgBPJ5EI5OGUuxQIQIE4e/WnuvAArDrbl5uCrBMZYAEQgIoAuKv/Vl1sAVoMA+5/ucwBAmjJxmn1p7rYArRqd87PgVsHgwI7EAABtAlAs/rTcUw6pDH1gACMJgX3v2zGAQE05GNa/ek0lvKLNiAAMwFoCDXl8dacJrd+7sTUyb2c/Yf2pVzUCglrs79dp4tVTZ+0whmALRUck6cQovlK9NBXfzJ+02fOQo2zw/pF5LWyEoBp9afV6mdvnFUKu2YCGB/uGYm51mGpbeXRYTqE1X/Axl9MYjsvARhif1JY3T3ytRKAJVk6JoG+Z/9nr71CzL5/7/ZCLP9+7LGviX8/dl+HC3TirgsJa/MSwOUzmypY5WoFAtiJjvXz33RVmuIYdcp320PN4uAfXj1uQv5pau+G3e8I7a7M+oUYP4GTjQA4saqSACo9BMR6Q1Kj3iW6/2TwHKNvTuljH/qgeOHU6vQsifj6+hR2yW0+AmBkqkEAl7SacT+C1gRKugPAx/DlxB5/9BHxxJEjU/OsNSRMwXlZCICz+tPkQACTbD8l/oRY8hF4Ke5/iOHbCAAHw3w0Q10HBBAPyygteZ2TaPRcgvsfw/hpSjoPAAQQRdXGjeQhAIb7Dw9gsvpbdkpsn93u2v2PZfyEBsX/lAdQPEXsocczw+5aSk4AXPcfBGD+8m97ZW+HS/JOO/quXVfP0UduEbStF/PR7ASAACKBnJ4ANPGsylWtPQdgOiWp+q4eHZTp0uCbOhhz5W+2qySASneGItn8jmbSEwDT/YcHML7Sy3hOIoUCxGgzlfHT2EAAMSSkbyMpAbi4/1oCoLPvlXwRqI+v9KY0ftIJ3VmAWnQirfknTgLqCIDiVZXrqjv7XYuw+3alF8X7FPenfEAAKdFNTQAO7j9NEwSg3v/XEWZa1bC3niLp1+4Vh4HscggpkTYE0MS0uq0qEID+AFDX23ttJcux+lOfIIAQ87bXTUYArvG/yQOo6eBHX77wk2P1NxFATTphN2P/EtkJwHRSzXDIpap9X10uoKRQYOVrv83SOorhb7zjDnFo8WFWeW4IAALwgnOqUjoCcIz/5chKvj8tDuT2Vkr3Ajjuf/sU3z9+69vipjvm7JNvlcBxYGfInCpkJwBbLKskgAoPfpR8JsDk/pPh0xt87dd4yfiJBFwfEIArYm7l0xGAIgHIeVMNl4JsCbDkm35M7r/h/L64968WnUMBHQGcWX97YTQa9fdeADc7TVY6CQH4JADlDLX3wBV0i0oyaUwaNr0RaPOgko+Nsff/+JFHxBOPTr/HT2NzDQV8dgGe+/Kxp6iv55588vfoz5975zvfJXH5vzffPP/rt956nv7/5dPfWZ297ba1T33u6NHUuJXaflYC4Lyqqk0E0i0wI7EsgWzejFsquL7j0rn/HA/Kt09uPW72X3eAxzUU4BAAEebilx6/mwz+lTMvbRs7d05U7pob9o+JoTZSSEMADi8AqYSkSQROF21eDbUpVumSTBeh5y47duvlsylWxS6x0iYy0z2AHAJNPScuAZhCARcSMBEArfTPfPaz8//54x9dlWLeNZBCVgK4/Z7HxNn/utIqq4DroIvdLjQY9qUrvg03AZWyBcjd/iMhm0IBbj5A5UlcuW+fuHLfVer7Aq3aFVbgzj++/9W/OPqF94S1Uk7tNASg2QJ88NlXxNP/dM46+wACoLaLIgHOdxDpXQdTuRJcfwKWs/3XFq72Yk8hWElBU32rIiUq8Cvv/dVXv/6D/xgECRRJACQ33W4AR6alvDzEMf7JfJYVH//YnmrXiT85EK77H4sEKIz4xB98RLx5fpyz83raZw+Utwx7tPyhD39k5e+On1jwqFpUlWwEQIK4//NfFYcfPs0GgBKC6+eOqz8YamqlgB0DB+M34lFC3B9KAKZ8ALVN4QA9N83NjQ8L6c4ScBRHGvy9i4vWg0fUzwurW9eO63YtTH2WstBwcNGVyUoAtAU0//v/6jXe5gcW6RNiG69/T0sMXQsmlvGXEvf7uv9NQdtIwEspJpXI6Om4sSSQkLZcSKFrPQuZp6ybhgAUh4Bk5teXAFSTLfHUoM34tZ9Ab02wlLhfDssn/m/LzJQU9FFm0inOSu/TdrPOkO8kyE4AFAKsnX0jVCbj1Z/yBFNPx8eGTdt40qg5Sc6SXH/C2Df+b8sn1BPwXe1l7N/+0EhITgAegMaMVQdZpAdAuwCcnQAbQ2gJoMP43yWTbzrrUJLrL+Xgsv1nk50PCfiu9iH5BNs8QAAeBECrv0siUCcE7S5BlwSg2cdXGbTOCyjN9Y8R/+tkaAsJQlZ71QtJNoN2/R0EoCMAxTmA5umvGHmAEq8Q173Gq9vGIy+GEprNp5RrvptjiuX+m4jgxVOntn8OSejZSMXVyHXlDzzwwENDeIcgTQ7AQgAx8gBFJgA1HkAp+/i+yh/T/fcdg61eSle/3fdQjJ/mlY0AqLMz62+PsQzNA6SM/1uruNOpQlcPwKbUJfweI/ufeh6+qz6d9Xcd2/W337Y8hJVfzjsNAWhWwlgEYMmiOxltUwGUBjx5C5Hz9uEQCSC1++9qgM3yLqs+Hd+9+5OfXPnR98++PCQDDsEvnQegIYDmu+AheQDONprLOwE6w90BLiO5ODQCKHn156760vA//NF7/ijUWIZYP6sH0CSA0DCARQKW1XuybTcvhFiyCtdCAGPjH4k5sSmovR1PX3MAJa7+3FUfhm/V6HGBNASwZ2ZeXBSX3n2fjKX5CmgWArgU6IwvE2m68axVv4mhhgA47fSRAGKu/mS0PheCtlWY+2bgUF7U4ZlwWKkkBEBD0t1qI/MAVCYkDKD6zi8LkUdAF3FoVmojlC0C4Bg+tVfivj5HZWKu/tJdl/v61D/33D53xac2Kak3tCQdR1YhZVISAN3OM+VaNwkg1AuQE3cmAh/EdokFcXHbvbeHDJM+SjvSy5l6zNWf+lN+4bfhFarGROcCXI7pYtXnSHa6THYCaN8EE+oFNKcUgwi4L+tw4C7xSC9n3DFXfxsBcMZjK7P42ONPI8lnQ0n9ezIC0IUBbQKI5QWEEoE0VsPXidgI99XwaYKxP/fNzdazwW0UHNLNPD7zj1EnLQForgZLEQaowOB4BLTiz+y9bvtz5SEE0GfDHxM248pvV6VLQQBk+Lf/1m9+Afv5rtLIGAJMPABlHiCHF9Ccqm7LUGWwrgRAbdBT4hl+V/WI7fpT/80LNlzj+vb4sbXnKlF7+bQeAGM7kIaYIgzQeQT08s1lV9Cqf60SHS4B9H21b08+tutvUj3yCujhXMP1y+9+94X7lj59AjG+3Zh9SiQlAF0egP49txfgAo4udGiHCy5tllw2p/HrcNDtFLxv//5njp9+/mDJ+PV5bDkIgBUGEIgxdwRiCEWGDkNy89u4pIj7XbHX5QmGdge/Ky45yicngL56ATnA77qPEoyfMMDq350m5CIApRdA027uCOTMB3QHeRk9l2L8WP271YcsBODiBZQYCnQrovi9lxDz06xMW4SI/ePLXdViTgJgewGx7g3MA2G/einF+E2uP2L/fDqVjQBcvYBcW4P5oO6+p5KM3/Rm35n1txdGo9FK94gNfwS5CUDrBai+FgsSiKeAKQ75kBHTBZ6HFh92Gihcfye4khbOSgATLwAkkFSkOxtPseq3X9HlfurbFvfjjb6MijHpKjsBmEIB+g2eQDwlSGH8utWbQwK29wKGcM9+POnlaakbAtAcEZZTbl4dJv8NiUG+QqQwfOrd9kUfldzkqG3GP6SrtvmS6r5kJwRgCwXod50yxfimQPewpxkBGT498s8UvdgMWSU3Wx24/ikkxWuzMwIIIQEkB6fj/NSG3+zRZtBNErCVhfHzDDVVqU4JgEMCutiSQoKnv/rDKF8aTgVujnZTufu2sdsMm0jA9n0+GL8N5fS/d04AISRAdWv0BnK4+hzVs5GAqQ0YPwfh9GWKIIAxCWhuD5IQ2D4PXUNuoKvV3qSGPiQA409v2NweiiEADgmYkoP021DDghINv6lg3Pv6qQ6Mn2uaecoVRQCccIDK2Pach0AEZPSz7987vqevDw+HBLDVV54kiyOAMQlYzglQGVtIIKHuExn0zejb6mwiAaz85Rk/jahIAtgmgU2xpPrWXhNKFyIYJw0L2jmQybw+rfQ2NTZ9BAQkYEMv/+/FEoCEgvsJLltY0IZWegYyd5AD+iEafBs322lBfMQjh6bx+yieALh5AZewQAUPEcKYDL7/+vg/X2KQMTut6uOx9yiO56uNuaSJBMhjO/btf+uF3sXCo+R2eiOIcV6AERKEEoFOWJIg2r/3JUmXWwlNJIBQILc09P31hgC2QwJGglCW5eYHyhHHsEaiIwF4AeXIuXcE4JobSOURlCPCskei2xnAlmAZcustAYzja4ewgMq7JgrLEJH7KMjo6KHbeuhxvbHHvUd9DZ0XgDAgJsr+bfWaAHy8gRo8AtWqS+TXFRmotgbxZV9/o41ZcxAEsE0ElvcJ2sANNUdgOpDThRekGs81N+w//5XvPv9LMZUZbbkjMCgC8AkLhugRcI7lmm7vcVcjcw0QQGxE47U3OALY9gYc8wOy3hC8Ag4B0HxzkYAqBIAHEM+IQ1oaLAHEIAJKot00Nzd+76BPD72i++KpU+M7/GxPDhJQEQA+/mGTTJ7fB08AoUTQDBHk3/OIJk4vtvf1idyIBFI9uv6xC5AKcbd2qyGAHURwUcwLIZbcoNoqLb2BexcXe+MZLH3so+Lrzz6rnW5KEsA5AB8ty1enOgJoQut6jkAlFpkzKNE76DoU0K3+iP/zGbitp6oJIEZ40AaYCEHmDnKTAsX8L6yusuP/9tjbn2q3KY/pd3z+KwS9fHVBAC2sY3gFKlKgf2sSQwg5SEOX/XATfja1ihUK2PIO+AKQTRL5fgcBaLAeEwE9zDcQQ0Vm2mngZPND+5eEFJoQtBn/+/bvf+b46ecPxhgv2ghHAATAwHDbK9gihC1i6O+zfOCBB84f/+IXP6+aQkgYYDt/gMx/eUoDAvCQSU8JYXntrQ36MrP4swN3nvzWc/+sJDIfAmh/LVgFKYzfQ9EyVAEBBIK8HSpcHD0lxOZVgc3Frr4sdomVtQsbK7Lhz3zi8GHd6u/znoDtCjDqF8YfW6zx2gMBRMLybw4/+JMT//ClKQK4ct8+8dPXXovUi6GZkVgRm2J89E+u9KrSd938gf9+5cxL74rh/tvifRh/erGH9gACCEWQ0gKbm/M37H7HSZNRNRN58u8vrp4aV/npa6/aSYIMXD4TQx//b2uFN03H5Pq7rv4c48cFoBGUK3ETIIAIAOtWf65R6a7Sjnlg5sAtH3j6By+9dLdqutxxyro246dxX3/7bcuf+tzRoxHgRRMJEQABRAB39vKZTV+X2mRMZ9bfXhiNRpdW/oCx6sbouvdvy/Tjoo8AIXVQFQQQCLpuZeWsqibjj5k403koNHXu24BI9gUqSqHVQQCBgvF1/00GFfNV2Rhxv83lR7IvUIk6rA4CCACfk/zTNW9ypWMdlY2x5ccxfiT7ApSo46oggAAB6FZXm/uf60WZ0C0/m/Ej2RegPIVUBQEECMIn+Zcr7g91/ZHsC1CMHlUFAXgKyzf5Z/p6bizXP8aWH77y66kYPasGAvAUmG/yz2RYNBRKAL58+jurvm/MmfISLlt+prMJtMfPgQ3nADgodVsGBOCJv4/7T13ZXOvmcHzIIMaWH43BRlQc2GJuZXL6Qxl3BEAA7pgJX/efuuLsp7eHxDWk0Li/2W8MAqD2uGP3EAOqREAABOABou/qL7ty8QJkHdtWW4y4vwmFbQfABTaQgAtaecuCABzxDln9ZVe+d/eZkoQ6UqI+fd7x9/VWdHDGSnA6igvFLQiAABxVxDf5Z+qGVlt6nnj0iHE0OiN67svHnjpy3yHlNVsuiT9d5zG8ARCAo6JlKg4CcAQ61P23dWe6ytvXA6A+bYeTbOOi3yVRmcrqLigFAXAQzl8GBOCAuc79l1eBH1p82KE1fVHdimsyItOpv2ZPqT8TrstvgACiqEb0RkAADpCaXG3ZTKyVVhUOmIyIM7b2VGmssb99CAJwUKgCioIAHIVwzwd/Y5NzTXeIcfl4AHQ34Z2H7n9KdS2ZbYoxPRgQgA3tsn4HATjKw3Wllcbl0k1IHE0nAe+69eaDutt/bOOQIYKtnO53XSITIYAvomnrgQA88DWdtvNojl3F1YgoZ3H9LbfO+XgF7EExC7qOndksigUiAALwBJBW2j+/68CS7n59z2aN1UKMyHRQKMVY222GjD3H+GrtAwQQQfI5VtpYF4TmGGsb0lhjjyAqNNFCAAQQUSXIK/jMQ4e9EnGmYaQyoBxeQaqxRxRb1U2BABKJP5Zx5TAgujps8+Lmg7FzBTnGnkh81TQLAkgsavIK/vZPH5r17Sb3O/VEBr5jbdfLPfZY466pHRBATdLGXIEAcgDQASAABCQC8ACgC0CgYgRAABULH1MHAiAA6AAQqBgBEEDFwsfUgQAIADoABCpGAARQsfAxdSAAAoAOAIGKEQABVCx8TB0IgACgA0CgYgRAABULH1MHAiAA6AAQqBgBEEDFwsfUgQAIADoABCpGAARQsfAxdSAAAoAOAIGKEQABVCx8TB0IgACgA0CgYgRAABULH1MHAiAA6AAQqBgBEEDFwsfUgQAIADoABCpGAARQsfAxdSAAAoAOAIGKEQABVCx8TB0IgACgA0CgYgRAABULH1MHAiAA6AAQqBgBEEDFwsfUgQAIADoABCpGAARQsfAxdSAAAoAOAIGKEQABVCx8TB0IgACgA0CgYgRAABULH1MHAv8Ppd9S4kEJYVoAAAAASUVORK5CYII=');

  const handleChange: UploadProps['onChange'] = (info: UploadChangeParam<UploadFile>) => {
    if (info.file.status === 'uploading') {
      setLoading(true);
      return;
    }
    if (info.file.status === 'done') {
      // Get this url from response in real world.
      getBase64(info.file.originFileObj as RcFile, (url) => {
        setLoading(false);
        setImageUrl(url);
      });
    }
  };

  const uploadButton = (
    <div>
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      {/* <div style={{ marginTop: 8 }}>Upload</div> */}
    </div>
  );

  return (

    <PageContainer content={<Breadcrumb>
      <Link to="/service/method">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      {!marketServices.get(type.type) ? <></> : <Card bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={marketServices.get(type.type)?.avatar}
            title={marketServices.get(type.type)?.title}
            description={marketServices.get(type.type)?.description}
          />
        </Card>}>
        <ProForm
          submitter={{
            searchConfig: {
              submitText: '保存',
            },
            resetButtonProps: {
              style: {
                // 隐藏重置按钮
                display: 'none',
              },
            },
            submitButtonProps: {
              style: {
                marginLeft: '-8px',
              },
            },
          }}
          title="填写应用信息"
          formRef={formRef}
          onFinish={async (values) => {
            const response = await saveOrUpdateAuthsaurAdmin({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                url: values?.url,
              }
            });
            if (response.success == true) {
              notification.success({ message: '添加成功' });
              history.push('/service/list');
              return true;
            } else {
              notification.error({ message: '添加失败:' + response?.msg || '' });
              return false;
            }
          }}
        >
          <Card
            bordered={false}
            bodyStyle={{ padding: '24px 0 0px' }}
            headStyle={{ padding: '0px 0 0px' }}
            title={<>基础配置</>}

          // extra={<></>}
          >
            <Row gutter={48}>
              <Col xs={24} lg={12}>
                <ProFormText name="id" hidden disabled />
                <ProFormText label="应用类型" name="type" hidden disabled />
                <ProFormText
                  label="应用名称"
                  name="name"
                  initialValue={'Authsaur控制台'}
                  allowClear={false}
                  rules={[{ required: true, message: '请输入收款人姓名' }]}
                  placeholder="请输入应用姓名"
                />
                <ProFormText
                  label="访问地址"
                  allowClear={false}
                  name="url"
                  rules={[{ required: true, message: '请输入地址' }]}
                />
              </Col>
              <Col xs={24} lg={12}>
                <ProFormItem
                  label="图标"
                  name="logo"
                >
                  <Upload
                    name="avatar"
                    listType="picture-card"
                    className="avatar-uploader"
                    showUploadList={false}
                    customRequest={(option: any) => { option.onSuccess(); }}
                    beforeUpload={beforeUpload}
                    onChange={handleChange}
                  >
                    {imageUrl ? <img src={imageUrl} alt="avatar" style={{ width: '100%' }} /> : uploadButton}
                  </Upload>
                </ProFormItem>
              </Col>
            </Row>
            <Row gutter={48}>
              {/* <Col xs={24} lg={12}>
                  
                </Col> */}
              <Col xs={24} lg={12}>
                {/* <ProFormTextArea
                    label="描述"
                    name="description"
                    fieldProps={{ rows: 5 }}
                    rules={[{ required: false, message: '请输入收款人姓名' }]}
                  /> */}
              </Col>
            </Row>
          </Card>
        </ProForm>
      </Card>}
    </PageContainer>
  );
};

export default Page;
