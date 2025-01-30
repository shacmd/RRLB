module de.unihannover.roundrobinloadbalancer {
    requires java.base;          // Included by default
    requires java.net.http;      // For networking

    exports de.unihannover.client;
    exports de.unihannover.loadbalancer;
    exports de.unihannover.server;
}


