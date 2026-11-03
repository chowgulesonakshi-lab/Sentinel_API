import axios from "axios";

const API_URL = "http://localhost:8080/endpoints";

const getAuthHeader = () => ({
    headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
});

export const getEndpoints = async () => {
    const response = await axios.get(API_URL, getAuthHeader());
    return response.data;
};

export const createEndpoint = async (endpoint) => {
    const response = await axios.post(API_URL, endpoint, getAuthHeader());
    return response.data;
};

export const updateEndpoint = async (id, endpoint) => {
    const response = await axios.put(`${API_URL}/${id}`, endpoint, getAuthHeader());
    return response.data;
};

export const deleteEndpoint = async (id) => {
    await axios.delete(`${API_URL}/${id}`, getAuthHeader());
};

export const triggerEndpointCheck = async (id) => {
    const response = await axios.post(`${API_URL}/${id}/trigger`, {}, getAuthHeader());
    return response.data;
};