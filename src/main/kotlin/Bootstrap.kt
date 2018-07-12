package leia

import leia.http.KtorServer
import leia.http.ServerFactory
import leia.logic.Resolver
import leia.logic.ResolverAtom
import leia.logic.SourceSpecsResolver
import leia.registry.TomlRegistry
import leia.sink.CachedSinkProviderFactory
import leia.sink.DefaultSinkProviderFactory
import leia.sink.SinkProvider
import leia.sink.SinkProviderAtom
import leia.sink.SpecSinkProvider
import se.zensum.leia.config.SinkProviderSpec
import se.zensum.leia.config.SourceSpec
import se.zensum.leia.config.TomlConfigProvider

fun run(sf: ServerFactory, resolver: Resolver, sinkProvider: SinkProvider) {
    val res = sf.create(resolver, sinkProvider)
}

fun bootstrap() {
    val reg = TomlRegistry(".")

    val spf = CachedSinkProviderFactory(DefaultSinkProviderFactory())
    val sp = SinkProviderAtom(SpecSinkProvider(spf, emptyList()))
    reg.watch("sink-providers", { SinkProviderSpec.fromMap(it) }) {
        sp.set(SpecSinkProvider(spf, it + SinkProviderSpec(
            "foo",
            true,
            "null",
            emptyMap()
        )))
    }

    val resolver = ResolverAtom(SourceSpecsResolver(listOf()))
    reg.watch("routes", { SourceSpec.fromMap(it) }) {
        resolver.set(SourceSpecsResolver(it))
    }

    reg.forceUpdate()

    run(
        KtorServer,
        resolver,
        sp
    )
}

