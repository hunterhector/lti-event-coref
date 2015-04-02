#!/usr/bin/env bash
mvn -pl event-coref-core exec:java -Dexec.mainClass="edu.cmu.lti.event_coref.pipeline.EndToEndCollectionResolver"
