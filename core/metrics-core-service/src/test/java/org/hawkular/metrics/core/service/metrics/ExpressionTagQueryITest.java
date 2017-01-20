/*
 * Copyright 2014-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hawkular.metrics.core.service.metrics;

import static java.util.Arrays.asList;

import static org.hawkular.metrics.model.MetricType.AVAILABILITY;
import static org.hawkular.metrics.model.MetricType.COUNTER;
import static org.hawkular.metrics.model.MetricType.GAUGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawkular.metrics.core.service.tags.ExpressionTagQueryParser;
import org.hawkular.metrics.model.DataPoint;
import org.hawkular.metrics.model.Metric;
import org.hawkular.metrics.model.MetricId;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import rx.Observable;

/**
 *
 * @author Stefan Negrea
 */
public class ExpressionTagQueryITest extends BaseMetricsITest {

    @Test
    public void tagValueSearch() throws Exception {
        String tenantId = "jsonT1Tag";

        createTagMetrics(tenantId);

        ExpressionTagQueryParser test = new ExpressionTagQueryParser(dataAccess, metricsService);

        List<Metric<Double>> gauges = test.parse(tenantId, GAUGE, "a1 ='abc'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m1");

        gauges = test.parse(tenantId, GAUGE, "a1 != 'abc'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m2", "m3", "m4", "m5");

        gauges = test.parse(tenantId, GAUGE, "a1 ='abc' OR a2='defg'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m1", "m6");

        gauges = test.parse(tenantId, GAUGE, "a1 ='11' OR a2 = '22'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges);

        gauges = test.parse(tenantId, GAUGE, "a1='defg' AND (a2='jkl' OR a2='xyz')").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m4", "m5");

        gauges = test.parse(tenantId, GAUGE, "a1 ='defg' AND (a2 in ['jkl', 'xyz'])").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m4", "m5");

        gauges = test.parse(tenantId, GAUGE, "a1 ='defg' AND (a2 notin ['jkl'])").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m5");

        gauges = test.parse(tenantId, GAUGE, "hostname ='web.*'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "mA", "mB");

        gauges = test.parse(tenantId, GAUGE, "hostname ='web.*' or a1='*'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m1", "m2", "m3", "m4", "m5", "mA", "mB");

        gauges = test.parse(tenantId, GAUGE, "a1 ='abc' and a1='abc'").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m1");

        gauges = test.parse(tenantId, GAUGE, "a1=abc or a1=jkl").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m1", "m3");

        gauges = test.parse(tenantId, GAUGE, "a1=defg AND (a2 in [jkl, xyz])").toList().toBlocking()
                .lastOrDefault(null);
        assertMetricListById(gauges, "m4", "m5");
    }

    private <T> void assertMetricListById(List<Metric<T>> actualMetrics, String... expectedMetricIds) {
        assertEquals(actualMetrics.size(), expectedMetricIds.length);
        for (String expectedMetricId : expectedMetricIds) {
            boolean found = false;
            for (Metric<T> actualMetric : actualMetrics) {
                if (actualMetric.getId().equals(expectedMetricId)) {
                    found = true;
                    break;
                }
            }

            assertTrue(found, "Metric " + expectedMetricId + " was not found in the list of returned metrics.");
        }
    }

    protected Map<String, Metric<?>> createTagMetrics(String tenantId) throws Exception {
        ImmutableList<MetricId<?>> ids = ImmutableList.of(
                new MetricId<>(tenantId, GAUGE, "m1"),
                new MetricId<>(tenantId, GAUGE, "m2"),
                new MetricId<>(tenantId, GAUGE, "m3"),
                new MetricId<>(tenantId, GAUGE, "m4"),
                new MetricId<>(tenantId, GAUGE, "m5"),
                new MetricId<>(tenantId, GAUGE, "m6"),
                new MetricId<>(tenantId, GAUGE, "mA"),
                new MetricId<>(tenantId, GAUGE, "mB"),
                new MetricId<>(tenantId, GAUGE, "mC"),
                new MetricId<>(tenantId, GAUGE, "mD"),
                new MetricId<>(tenantId, GAUGE, "mE"),
                new MetricId<>(tenantId, GAUGE, "mF"),
                new MetricId<>(tenantId, GAUGE, "mG"),
                new MetricId<>(tenantId, GAUGE, "gl1"),
                new MetricId<>(tenantId, GAUGE, "gl2"),
                new MetricId<>(tenantId, GAUGE, "gl3"),
                new MetricId<>(tenantId, AVAILABILITY, "a1"),
                new MetricId<>(tenantId, COUNTER, "c1"));

        @SuppressWarnings("unchecked")
        ImmutableList<ImmutableMap<String, String>> maps = ImmutableList.of(
                ImmutableMap.of("a1", "abc", "a2", "jkl"),//m1
                ImmutableMap.of("a1", "defg"),//m2
                ImmutableMap.of("a1", "jkl"),//m3
                ImmutableMap.of("a1", "defg", "a2", "jkl"),//m4
                ImmutableMap.of("a1", "defg", "a2", "xyz"),//m5
                ImmutableMap.of("a2", "defg"),//m6
                ImmutableMap.of("hostname", "webfin01"),//mA
                ImmutableMap.of("hostname", "webswe02"),//mB
                ImmutableMap.of("hostname", "backendfin01"),//mC
                ImmutableMap.of("hostname", "backendswe02"),
                ImmutableMap.of("owner", "hede"),
                ImmutableMap.of("owner", "hades"),
                ImmutableMap.of("owner", "had"),
                ImmutableMap.of("label", "test:test,test1:test2,test3:test4"),
                ImmutableMap.of("label", "test1:test2,test3:test4"),
                ImmutableMap.of("label", "test:,test1:test2"),
                ImmutableMap.of("a1", "jkl"),
                ImmutableMap.of("a1", "5"));
        assertEquals(ids.size(), maps.size(), "ids' size should equal to maps' size");

        // Create the metrics
        List<Metric<?>> metricsToAdd = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            if(ids.get(i).getType() == GAUGE) {
                metricsToAdd.add(new Metric<>((MetricId<Double>) ids.get(i), maps.get(i), 24, asList(
                        new DataPoint<>(System.currentTimeMillis(), 1.0))));
            } else {
                metricsToAdd.add(new Metric<>(ids.get(i), maps.get(i), 24));
            }
        }

        // Insert metrics
        Observable.from(metricsToAdd)
                .subscribe(m -> metricsService.createMetric(m, false).toBlocking().lastOrDefault(null));

        return Observable.from(metricsToAdd).toMap(e -> e.getId()).toBlocking().lastOrDefault(null);
    }
}