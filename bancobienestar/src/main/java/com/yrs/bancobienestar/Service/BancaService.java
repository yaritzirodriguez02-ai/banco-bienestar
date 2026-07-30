package com.yrs.bancobienestar.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yrs.bancobienestar.Modelo.CuentaEntity;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;
import com.yrs.bancobienestar.Repository.CuentaRepository;
import com.yrs.bancobienestar.Repository.MovimientoCuentaRepository;
import com.yrs.bancobienestar.Repository.SolicitudCreditoRepository;
import com.yrs.bancobienestar.Repository.UsuarioRepository;

@Service
public class BancaService {
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoCuentaRepository movimientoRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final PasswordEncoder passwordEncoder;

    public BancaService(UsuarioRepository usuarioRepository,
            CuentaRepository cuentaRepository,
            MovimientoCuentaRepository movimientoRepository,
            SolicitudCreditoRepository solicitudCreditoRepository,
            @Lazy PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferirMonto(String clabeOrigen, String clabeDestino, Double monto, String descripcion) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (clabeOrigen.equals(clabeDestino)) {
            throw new IllegalArgumentException("La cuenta de destino no puede ser la misma que la de origen.");
        }
        CuentaEntity origen = cuentaRepository.findByClabe(clabeOrigen)
                .orElseThrow(() -> new RuntimeException("La cuenta de origen no existe."));
        CuentaEntity destino = cuentaRepository.findByClabe(clabeDestino)
                .orElseThrow(() -> new RuntimeException("La cuenta de destino no existe."));
        if (origen.getSaldo() < monto) {
            throw new RuntimeException("No cuentas con saldo suficiente para esta operación.");
        }
        origen.setSaldo(origen.getSaldo() - monto);
        cuentaRepository.save(origen);
        destino.setSaldo(destino.getSaldo() + monto);
        cuentaRepository.save(destino);
        MovimientosEntity movimiento = new MovimientosEntity();
        movimiento.setCuentaOrigen(clabeOrigen);
        movimiento.setCuentaDestino(clabeDestino);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipo("Transferencia");
        movimiento.setEstadoMovimiento("AUTORIZADO");
        movimientoRepository.save(movimiento);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferirDesdeUsuario(String username, String clabeDestino, Double monto, String descripcion) {
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una cuenta bancaria asignada.");
        }
        String clabeOrigen = usuario.getCuentas().get(0).getClabe();
        transferirMonto(clabeOrigen, clabeDestino, monto, descripcion);
    }

    // MÉTODO: Depositar/Recargar saldo a la propia cuenta
    @Transactional(rollbackFor = Exception.class)
    public void depositarAUsuario(String username, Double monto, String descripcion) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a cero.");
        }

        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una cuenta bancaria asignada.");
        }

        CuentaEntity cuenta = usuario.getCuentas().get(0);

        // Sumar al saldo de la cuenta del usuario
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuentaRepository.save(cuenta);

        // Registrar el movimiento en el historial
        MovimientosEntity movimiento = new MovimientosEntity();
        movimiento.setCuentaOrigen("DEPOSITO-ATM");
        movimiento.setCuentaDestino(cuenta.getClabe());
        movimiento.setMonto(monto);
        movimiento.setDescripcion((descripcion == null || descripcion.trim().isEmpty()) ? "Depósito a cuenta" : descripcion);
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipo("Deposito");
        movimiento.setEstadoMovimiento("AUTORIZADO");

        movimientoRepository.save(movimiento);
    }

    @Transactional(rollbackFor = Exception.class)
    public UsuarioEntity crearClienteConCuenta(String nombre, String username, String password, Double saldoInicial) {
        if (usuarioRepository.findByUserName(username).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está registrado.");
        }
        if (usuarioRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("El nombre de cliente ya existe.");
        }
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUserName(username);
        usuario.setNombre(nombre);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("CLIENTE");
        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);
        String clabe = generarClabeUnica();
        CuentaEntity cuenta = new CuentaEntity();
        cuenta.setClabe(clabe);
        cuenta.setSaldo(saldoInicial);
        cuenta.setUsuario(usuarioGuardado);
        cuentaRepository.save(cuenta);
        List<CuentaEntity> list = new ArrayList<>();
        list.add(cuenta);
        usuarioGuardado.setCuentas(list);
        return usuarioGuardado;
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudCreditoEntity guardarSolicitudCredito(String username, Double monto, String firmaBase64) {
        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        SolicitudCreditoEntity solicitud = new SolicitudCreditoEntity();
        solicitud.setUsuario(usuario);
        solicitud.setMontoSolicitado(monto);
        solicitud.setFirmaBase64(firmaBase64);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFecha(LocalDateTime.now());
        
        return solicitudCreditoRepository.save(solicitud);
    }

    @Transactional(rollbackFor = Exception.class)
    public void aprobarSolicitudCredito(Long idSolicitud) {
        SolicitudCreditoEntity solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada."));
        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("Esta solicitud ya fue procesada anteriormente.");
        }
        UsuarioEntity usuario = solicitud.getUsuario();
        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El cliente no tiene una cuenta asignada.");
        }
        CuentaEntity cuenta = usuario.getCuentas().get(0);
        cuenta.setSaldo(cuenta.getSaldo() + solicitud.getMontoSolicitado());
        cuentaRepository.save(cuenta);
        MovimientosEntity movimiento = new MovimientosEntity();
        movimiento.setCuentaOrigen("CRÉDITO-BANCO");
        movimiento.setCuentaDestino(cuenta.getClabe());
        movimiento.setMonto(solicitud.getMontoSolicitado());
        movimiento.setEstadoMovimiento("AUTORIZADO");
        movimiento.setTipo("Deposito");
        movimiento.setDescripcion("Abono de Crédito Autorizado");
        movimiento.setFecha(LocalDate.now());
        movimientoRepository.save(movimiento);
        solicitud.setEstado("APROBADO");
        solicitudCreditoRepository.save(solicitud);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rechazarSolicitudCredito(Long idSolicitud) {
        SolicitudCreditoEntity solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada."));
        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("Esta solicitud ya fue procesada anteriormente.");
        }
        solicitud.setEstado("RECHAZADO");
        solicitudCreditoRepository.save(solicitud);
    }

    private String generarClabeUnica() {
        Random random = new Random();
        String clabe;
        do {
            StringBuilder sb = new StringBuilder("012");
            for (int i = 0; i < 15; i++) {
                sb.append(random.nextInt(10));
            }
            clabe = sb.toString();
        } while (cuentaRepository.findByClabe(clabe).isPresent());

        return clabe;
    }
    

    // Crear método para buscar todos los movimientos
    @Transactional(readOnly = true)
    public List<MovimientosEntity> todosMovimientos() {
        return movimientoRepository.findAll();
    }

    // Obtener movimiento por ID
    @Transactional(readOnly = true)
    public MovimientosEntity obtenerMovimientoId(Long id) {
        return movimientoRepository.findById(id).orElse(null);
    }

    // Método para modificar estado del movimiento
    // LÓGICA DE CANCELACIÓN E IMPACTO DE SALDOS 
    @Transactional(rollbackFor = Exception.class)
    public void actualizarMovimientos(Long id, String nuevoEstado) {
        MovimientosEntity movimiento = obtenerMovimientoId(id);
        if (movimiento == null) {
            throw new RuntimeException("El movimiento no existe en el sistema.");
        }

        String estadoActual = movimiento.getEstadoMovimiento() != null ? movimiento.getEstadoMovimiento().trim().toUpperCase() : "";
        String estadoNuevo = nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : "";

        // Verificamos si se está cancelando y si no estaba cancelado previamente
        boolean seEstaCancelando = estadoNuevo.contains("CANCEL");
        boolean yaEstabaCancelado = estadoActual.contains("CANCEL");

        if (seEstaCancelando && !yaEstabaCancelado) {
            revertirSaldoMovimiento(movimiento);
        }

        // Actualizamos el estado del movimiento en la base de datos
        movimiento.setEstadoMovimiento(estadoNuevo);
        movimientoRepository.save(movimiento);
    }

    // Método privado auxiliar que aplica las operaciones aritméticas según el tipo de movimiento
    private void revertirSaldoMovimiento(MovimientosEntity movimiento) {
        if (movimiento.getMonto() == null || movimiento.getMonto() <= 0) {
            return;
        }

        String tipo = movimiento.getTipo() != null ? movimiento.getTipo().trim().toUpperCase() : "";
        Double monto = movimiento.getMonto();


        // 1. PAGO / RETIRO:
        // Se cancela el cobro -> Se le SUMA (+ monto) al saldo del cliente origen.
        if (tipo.contains("PAGO") || tipo.contains("RETIRO")) {
            if (movimiento.getCuentaOrigen() != null) {
                CuentaEntity origen = cuentaRepository.findByClabe(movimiento.getCuentaOrigen()).orElse(null);
                if (origen != null) {
                    origen.setSaldo(origen.getSaldo() + monto);
                    cuentaRepository.save(origen);
                }
            }
        } 
        // 2. DEPÓSITO:
        // Se cancela el abono -> Se le RESTA (- monto) al saldo del cliente destino.
        else if (tipo.contains("DEPOSITO") || tipo.contains("DEPÓSITO")) {
            if (movimiento.getCuentaDestino() != null) {
                CuentaEntity destino = cuentaRepository.findByClabe(movimiento.getCuentaDestino()).orElse(null);
                if (destino != null) {
                    destino.setSaldo(destino.getSaldo() - monto);
                    cuentaRepository.save(destino);
                }
            }
        } 
      
        // 3. TRANSFERENCIA / TRANFERENCIA:
        // Se revierte la operación entre cuentas:
        // -> Cuenta Origen: Se le SUMA (+ monto) para devolverle sus fondos.
        // -> Cuenta Destino: Se le RESTA (- monto) para quitarle el abono por error.
        else if (tipo.contains("TRANFERENCIA") || tipo.contains("TRANSFERENCIA")) {
            // Reembolso a la cuenta que envió
            if (movimiento.getCuentaOrigen() != null) {
                CuentaEntity origen = cuentaRepository.findByClabe(movimiento.getCuentaOrigen()).orElse(null);
                if (origen != null) {
                    origen.setSaldo(origen.getSaldo() + monto);
                    cuentaRepository.save(origen);
                }
            }

            // Retiro a la cuenta que recibió por error
            if (movimiento.getCuentaDestino() != null) {
                CuentaEntity destino = cuentaRepository.findByClabe(movimiento.getCuentaDestino()).orElse(null);
                if (destino != null) {
                    destino.setSaldo(destino.getSaldo() - monto);
                    cuentaRepository.save(destino);
                }
            }
        }
    }

    // Eliminar movimiento por ID
    @Transactional(rollbackFor = Exception.class)
    public void eliminarMovimiento(Long id) {
        movimientoRepository.deleteById(id);
    }

    // Cambiar estado de la cuenta del cliente (ACTIVA / INACTIVA / SUSPENDIDA)
    @Transactional(rollbackFor = Exception.class)
    public void actualizarEstadoCuentaCliente(Long usuarioId, String nuevoEstado) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
            CuentaEntity cuenta = usuario.getCuentas().get(0);
            cuenta.setEstado(nuevoEstado);
            cuentaRepository.save(cuenta);
        }
    }

    // Eliminar cliente y sus cuentas asociadas
    @Transactional(rollbackFor = Exception.class)
    public void eliminarCliente(Long usuarioId) {
        usuarioRepository.deleteById(usuarioId);
    }

    // =======================================================
    // NUENOS MÉTODOS SOLICITADOS POR EL PROFESOR
    // =======================================================

    // 1. REGISTRAR UN MOVIMIENTO PROPIO (Ej. Starbucks, OXXO, etc.)
    @Transactional(rollbackFor = Exception.class)
    public void registrarGastoCliente(String username, String descripcion, Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto del gasto debe ser mayor a cero.");
        }

        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El cliente no posee una cuenta activa.");
        }

        CuentaEntity cuenta = usuario.getCuentas().get(0);

        if (cuenta.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente para realizar el pago.");
        }

        // Descontar saldo del cliente
        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuentaRepository.save(cuenta);

        // Guardar movimiento de gasto personal
        MovimientosEntity mov = new MovimientosEntity();
        mov.setCuentaOrigen(cuenta.getClabe());
        mov.setCuentaDestino("ESTABLECIMIENTO (" + descripcion + ")");
        mov.setTipo("Pago / Compra");
        mov.setDescripcion(descripcion);
        mov.setMonto(monto);
        mov.setEstadoMovimiento("AUTORIZADO");
        mov.setFecha(LocalDate.now());

        movimientoRepository.save(mov);
    }

    // 2. ABONAR / PAGAR A SU CRÉDITO
    @Transactional(rollbackFor = Exception.class)
    public void abonarACredito(Long creditoId, Double montoAbono, String username) {
        if (montoAbono == null || montoAbono <= 0) {
            throw new IllegalArgumentException("El monto a abonar debe ser mayor a cero.");
        }

        SolicitudCreditoEntity credito = solicitudCreditoRepository.findById(creditoId)
                .orElseThrow(() -> new RuntimeException("Solicitud de crédito no encontrada."));

        UsuarioEntity usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una cuenta configurada.");
        }

        CuentaEntity cuenta = usuario.getCuentas().get(0);

        if (cuenta.getSaldo() < montoAbono) {
            throw new RuntimeException("Saldo insuficiente para abonar al crédito.");
        }

        // Descontar de la cuenta principal del cliente
        cuenta.setSaldo(cuenta.getSaldo() - montoAbono);
        cuentaRepository.save(cuenta);

        // Registrar la transacción de abono
        MovimientosEntity mov = new MovimientosEntity();
        mov.setCuentaOrigen(cuenta.getClabe());
        mov.setCuentaDestino("CRÉDITO #" + creditoId);
        mov.setTipo("Abono Crédito");
        mov.setDescripcion("Abono a crédito folio #" + creditoId);
        mov.setMonto(montoAbono);
        mov.setEstadoMovimiento("AUTORIZADO");
        mov.setFecha(LocalDate.now());

        movimientoRepository.save(mov);
    }
}