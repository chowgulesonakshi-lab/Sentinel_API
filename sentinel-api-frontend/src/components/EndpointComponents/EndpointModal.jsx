import { useEffect, useState } from "react";
import {
    createEndpoint,
    updateEndpoint
} from "../../services/endpointsService";
import { getServices } from "../../services/apiServiceService";

function EndpointModal({
    isOpen,
    onClose,
    refreshEndpoints,
    selectedEndpoint
}) {

    const [services, setServices] = useState([]);

    const [endpoint, setEndpoint] = useState({
        method: "GET",
        url: "",
        timeoutMillis: 5000,
        enabled: true,
        apiServiceId: ""
    });

    useEffect(() => {
        if (!isOpen) return;

        loadServices();

        if (selectedEndpoint) {
            setEndpoint({
                method: selectedEndpoint.method,
                url: selectedEndpoint.url,
                timeoutMillis: selectedEndpoint.timeoutMillis,
                enabled: selectedEndpoint.enabled,
                apiServiceId: selectedEndpoint.apiServiceId
            });
        } else {
            setEndpoint({
                method: "GET",
                url: "",
                timeoutMillis: 5000,
                enabled: true,
                apiServiceId: ""
            });
        }
    }, [isOpen, selectedEndpoint]);

    const loadServices = async () => {
        try {
            const data = await getServices();
            setServices(data);
        } catch (error) {
            console.error(error);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;

        setEndpoint({
            ...endpoint,
            [name]: type === "checkbox" ? checked : value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            if (selectedEndpoint) {
                await updateEndpoint(selectedEndpoint.id, endpoint);
            } else {
                await createEndpoint(endpoint);
            }

            refreshEndpoints();
            onClose();

        } catch (error) {
            console.error(error);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">

            <div className="bg-white dark:bg-slate-900 rounded-2xl p-6 w-full max-w-lg border border-slate-200 dark:border-slate-700">

                <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-6">
                    {selectedEndpoint ? "Edit Endpoint" : "Add Endpoint"}
                </h2>

                <form
                    onSubmit={handleSubmit}
                    className="space-y-4"
                >

                    <select
                        name="apiServiceId"
                        value={endpoint.apiServiceId}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        required
                    >
                        <option value="">
                            Select Service
                        </option>

                        {services.map((service) => (
                            <option
                                key={service.id}
                                value={service.id}
                            >
                                {service.projectName} • {service.name}
                            </option>
                        ))}
                    </select>

                    <select
                        name="method"
                        value={endpoint.method}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                    >
                        <option>GET</option>
                        <option>POST</option>
                        <option>PUT</option>
                        <option>PATCH</option>
                        <option>DELETE</option>
                    </select>

                    <input
                        type="text"
                        name="url"
                        placeholder="Endpoint URL"
                        value={endpoint.url}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        required
                    />

                    <input
                        type="number"
                        name="timeoutMillis"
                        placeholder="Timeout (ms)"
                        value={endpoint.timeoutMillis}
                        onChange={handleChange}
                        className="w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        min="100"
                        required
                    />

                    <label className="flex items-center gap-3 text-slate-700 dark:text-slate-300">

                        <input
                            type="checkbox"
                            name="enabled"
                            checked={endpoint.enabled}
                            onChange={handleChange}
                        />

                        Enabled

                    </label>

                    <div className="flex justify-end gap-3 pt-2">

                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white"
                        >
                            {selectedEndpoint ? "Update" : "Save"}
                        </button>

                    </div>

                </form>

            </div>

        </div>
    );
}

export default EndpointModal;