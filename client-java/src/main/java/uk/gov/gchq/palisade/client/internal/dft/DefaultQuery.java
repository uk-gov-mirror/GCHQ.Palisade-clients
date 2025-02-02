/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.client.internal.dft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Query;
import uk.gov.gchq.palisade.client.QueryResponse;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;
import uk.gov.gchq.palisade.client.internal.model.PalisadeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static uk.gov.gchq.palisade.client.internal.request.PalisadeService.createPalisadeService;
import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * The default {@code Query} implementation for subname {@code dft}
 *
 * @since 0.5.0
 */
public class DefaultQuery implements Query {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQuery.class);

    private final DefaultSession session;
    private final String queryString;
    private final Map<String, String> context;

    /**
     * Creates a new query with the provided {@code session}, and query {@code info}
     *
     * @param session     The open session to the cluster
     * @param queryString The resource query string
     * @param context     Properties to be forwarded with the query
     */
    public DefaultQuery(final DefaultSession session, final String queryString, final Map<String, String> context) {
        this.session = checkNotNull(session);
        this.queryString = checkNotNull(queryString);
        this.context = new HashMap<>(checkNotNull(context));
    }

    @Override
    public CompletableFuture<QueryResponse> execute() {
        var palisadeService = createPalisadeService(b -> b
            .httpClient(session.getHttpClient())
            .objectMapper(session.getObjectMapper())
            .uri(session.getConfiguration().get(Configuration.PALISADE_URI)));

        var palisadeRequest = PalisadeRequest.Builder.create()
            .withUserId(session.getConfiguration().get(Configuration.USER_ID))
            .withResourceId(queryString)
            .withContext(context);

        LOGGER.debug("Executing query: {}", palisadeRequest);
        return palisadeService
            .submitAsync(palisadeRequest)
            .thenApply(response -> new DefaultQueryResponse(session, response));

    }

}
