const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/+$/, "");

export function subscribeToMonitoringEvents(onUpdate) {
    let active = true;
    let controller = null;
    let retryTimer = null;
    let retryDelay = 2000;
    let updateTimer = null;
    let latestUpdate = null;

    const connect = async () => {
        const token = localStorage.getItem("token");
        if (!active || !token) return;

        controller = new AbortController();

        try {
            const response = await fetch(`${API_BASE_URL}/events/stream`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    Accept: "text/event-stream",
                },
                signal: controller.signal,
            });

            if (response.status === 401 || response.status === 403) {
                console.warn("Monitoring event stream requires a valid login.");
                return;
            }
            if (!response.ok || !response.body) {
                throw new Error(`Event stream failed with HTTP ${response.status}`);
            }

            retryDelay = 2000;
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = "";

            while (active) {
                const { value, done } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                buffer = buffer.replace(/\r\n/g, "\n");
                let boundary = buffer.indexOf("\n\n");

                while (boundary !== -1) {
                    const frame = buffer.slice(0, boundary);
                    buffer = buffer.slice(boundary + 2);

                    let eventName = "message";
                    const dataLines = [];

                    for (const line of frame.split(/\r?\n/)) {
                        if (line.startsWith("event:")) {
                            eventName = line.slice(6).trim();
                        } else if (line.startsWith("data:")) {
                            dataLines.push(line.slice(5).trimStart());
                        }
                    }

                    if (eventName === "monitoring-update" && dataLines.length > 0) {
                        try {
                            latestUpdate = JSON.parse(dataLines.join("\n"));
                            if (updateTimer !== null) window.clearTimeout(updateTimer);
                            updateTimer = window.setTimeout(() => {
                                updateTimer = null;
                                const update = latestUpdate;
                                latestUpdate = null;
                                onUpdate(update);
                            }, 200);
                        } catch (error) {
                            console.error("Unable to process monitoring event:", error);
                        }
                    }

                    boundary = buffer.indexOf("\n\n");
                }
            }
        } catch (error) {
            if (!active || error.name === "AbortError") return;
            console.warn("Monitoring event stream disconnected; reconnecting.", error);
        }

        if (active) {
            retryTimer = window.setTimeout(connect, retryDelay);
            retryDelay = Math.min(retryDelay * 2, 30000);
        }
    };

    connect();

    return () => {
        active = false;
        if (retryTimer !== null) window.clearTimeout(retryTimer);
        if (updateTimer !== null) window.clearTimeout(updateTimer);
        controller?.abort();
    };
}
