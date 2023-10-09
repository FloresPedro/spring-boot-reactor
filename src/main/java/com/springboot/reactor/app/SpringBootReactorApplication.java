package com.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.springboot.reactor.app.models.Comentarios;
import com.springboot.reactor.app.models.Usuario;
import com.springboot.reactor.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// ejemploIterable();
		// ejemploFlatMap();
		// ejemploToString();
		// ejemploCollectList();
		// ejemploUsuarioComentariosFlatMap();
		// ejemploUsuarioComentariosZipWith();
		// ejemploUsuarioComentariosZipWithForma2();
		// ejemploZipWithRangos();
		// ejemploInterval();
		// ejemploDelayElements();
		// ejemploIntervaloInfinito();
		// ejemploIntervalDesdeCreate();
		ejemploContraPresion();
	}
	
	public void ejemploContraPresion() {
		
		Flux.range( 1, 10)
		.log()
		.limitRate(2)
		.subscribe(/*new Subscriber<Integer>() {
			
			private Subscription sct;
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				// TODO Auto-generated method stub
				this.sct = s;
				s.request(Long.MAX_VALUE);
				
			}

			@Override
			public void onNext(Integer t) {
				// TODO Auto-generated method stub
				log.info(t.toString());
				consumido++;
				if(consumido == limite) {
					consumido = 0;
					sct.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		}*/);
		//.subscribe(i -> log.info(i.toString())); //usando una interfaz 
		
	}
	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emmiter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				private Integer contador = 0;
				@Override
				public void run() {
					// TODO Auto-generated method stub
					emmiter.next(++contador);
					if(contador == 10) {
						timer.cancel();
						emmiter.complete();
						//return Flux.error(new InterruptedException("El contador llego a 10"));
					}
					
					if (contador == 5) {
						timer.cancel();
						emmiter.error(new InterruptedException("Error, se a detenido el flux en 5!"));
					}
				}
			}, 1000,1000);
		})
		//.doOnNext(next -> log.info(next.toString())) // Se puede manejar en el subscribe
		//.doOnComplete(() -> log.info("Hemos terminado lo infinito alv")) // Se puede trabajar en el tercer argumento del subscribe
		.subscribe(
				next -> log.info(next.toString()), 
				err -> log.error(err.getMessage()), 
				() -> log.info("Hemos terminado lo infinito alv"));
	}
	
	// Ejemplo de intervalo infinito
	public void ejemploIntervaloInfinito() throws InterruptedException {
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		//.doOnTerminate(() -> latch.countDown())
		.doOnTerminate(latch::countDown)
		.flatMap(i -> {
			if(i >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5"));
			} else {
				return Flux.just(i);
			}
		})
		.map(i -> "Hola "+i)
		.retry(2)// intenta ejecutar el flux 2 veces mas hasta si no lo logra falla y lanza el error
		.subscribe(s -> log.info(s), err -> log.error(err.getMessage()));
		
		latch.await();
	}
	
	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		//rango.subscribe();
		rango.blockLast();
	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(retraso, (ra,re) -> ra)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast();// para que sea una aplicacion bloqueante y se puedan visualizar los elementos que se van a segundo plano
		//.subscribe();
	}
	
	public void ejemploZipWithRangos() {
		Flux<Integer> rango = Flux.range(0, 4);// Se puede crear aparte o en el zipWith como esta comentado
		Flux.just(1,2,3,4)
		.map( i -> ( i * 2 ))
		.zipWith(/*Flux.range(0, 4)*/rango, (uno,dos) -> String.format("Primer Flux: %d, segundo Flux: %d", uno, dos))
		.subscribe(t -> log.info(t));
	}

	public void ejemploUsuarioComentariosZipWithForma2() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> monoComentarios = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pedro, ¿que tal?");
			comentarios.addComentario("Mañana me voy a veracruz");
			comentarios.addComentario("otro comentario");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(monoComentarios)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> monoComentarios = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pedro, ¿que tal?");
			comentarios.addComentario("Mañana me voy a veracruz");
			comentarios.addComentario("otro comentario");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(monoComentarios,
				(usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> monoComentarios = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pedro, ¿que tal?");
			comentarios.addComentario("Mañana me voy a veracruz");
			comentarios.addComentario("otro comentario");
			return comentarios;
		});

		usuarioMono.flatMap(u -> monoComentarios.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Pedro", "Flores"));
		usuariosList.add(new Usuario("Leonor", "Flores"));
		usuariosList.add(new Usuario("Efra", "Flores"));
		usuariosList.add(new Usuario("Sofia", "Texcahua"));
		usuariosList.add(new Usuario("Inocencia", "Cortes"));
		usuariosList.add(new Usuario("Leonor", "Castillo"));

		Flux.fromIterable(usuariosList).collectList().subscribe(lista -> {
			lista.forEach(item -> log.info(lista.toString()));
		});

	}

	public void ejemploToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Pedro", "Flores"));
		usuariosList.add(new Usuario("Leonor", "Flores"));
		usuariosList.add(new Usuario("Efra", "Flores"));
		usuariosList.add(new Usuario("Sofia", "Texcahua"));
		usuariosList.add(new Usuario("Inocencia", "Cortes"));
		usuariosList.add(new Usuario("Leonor", "Castillo"));

		Flux.fromIterable(usuariosList).map(
				usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("leonor".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				}).map(nombre -> {
					return nombre.toLowerCase();
				}).subscribe(u -> log.info(u.toString()));

	}

	public void ejemploFlatMap() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Pedro Flores");
		usuariosList.add("Leonor Flores");
		usuariosList.add("Efra Flores");
		usuariosList.add("Sofia Texcahua");
		usuariosList.add("Inocencia Cortes");
		usuariosList.add("Leonor Castillo");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("leonor")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(u -> log.info(u.toString()));

	}

	public void ejemploIterable() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Pedro Flores");
		usuariosList.add("Leonor Flores");
		usuariosList.add("Efra Flores");
		usuariosList.add("Sofia Texcahua");
		usuariosList.add("Inocencia Cortes");
		usuariosList.add("Leonor Castillo");

		Flux<String> nombres = Flux.fromIterable(usuariosList);// Flux.just("Pedro Flores","Leonor Flores","Efra
																// Flores","Sofia Texcahua","Inocencia Cortes", "Leonor
																// Castillo");
		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("leonor")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});
		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				log.info("Ha finalizado la ejecucion del observable con exito");
			}
		});
	}

}
