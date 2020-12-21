import Api from "./axios";

const createShipment = (body, token) => {
  console.log({ ...body });

  return Api.post(
    "/shipment/create",
    { ...body },
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
};

const getAllShipments = (token) =>{
    return Api.get("/shipment/all", {headers: { Authorization: `Bearer ${token}` },
});
}

const updateShipment = ({
  shipment_id,
  receiver,
  weight,
  boxColour,
  shipmentStatus,
  destinationCountry,
  sourceCountry
},
token
) => {return Api.patch(`/${shipment_id}`, {
  receiver,
  weight,
  boxColour,
  shipmentStatus,
  destinationCountry,
  sourceCountry
}, {
  headers: {Authorization: `Bearer ${token}`}
});

};

const deleteShipment = (
  shipment_id,
  token

) => {
  return Api.delete(`/${shipment_id}`, {
    headers: {Authorization: `bearer ${token}`}
  });
};

export { createShipment,updateShipment, getAllShipments, deleteShipment };

