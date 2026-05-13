// src/app/features/asesores/asesores.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AsesorService } from '../../core/services/asesor.service';
import { ToastService } from '../../core/services/toast.service';
import { Asesor, AsesorDTO } from '../../core/models';

@Component({
  selector: 'app-asesores',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './asesores.component.html',
  styleUrls: ['./asesores.component.scss']
})
export class AsesoresComponent implements OnInit {
  asesores: Asesor[] = [];
  loading = true;
  showModal = false;
  editMode = false;
  submitting = false;
  cargaMap: { [id: string]: number } = {};
  rankingMode = false;

  form: AsesorDTO = this.emptyForm();

  constructor(private asesorService: AsesorService, private toast: ToastService) {}

  ngOnInit() { this.cargarAsesores(); }

  cargarAsesores() {
    this.loading = true;
    this.asesorService.listar().subscribe({
      next: d => { this.asesores = d; this.loading = false; this.cargarCargas(); },
      error: (e: any) => { this.toast.error(e.message); this.loading = false; }
    });
  }

  cargarCargas() {
    this.asesores.forEach(a => {
      this.asesorService.obtenerCarga(a.id).subscribe({ next: r => this.cargaMap[a.id] = r.cargaTotal, error: () => {} });
    });
  }

  toggleRanking() {
    this.rankingMode = !this.rankingMode;
    this.loading = true;
    if (this.rankingMode) {
      this.asesorService.rankingPorCierres().subscribe({ next: d => { this.asesores = d; this.loading = false; }, error: (e: any) => { this.toast.error(e.message); this.loading = false; } });
    } else {
      this.cargarAsesores();
    }
  }

  abrirCrear() { this.form = this.emptyForm(); this.editMode = false; this.showModal = true; }
  abrirEditar(a: Asesor) { this.form = { id: a.id, nombre: a.nombre, contacto: a.contacto, especialidadZona: a.especialidadZona }; this.editMode = true; this.showModal = true; }

  guardar() {
    if (!this.form.id || !this.form.nombre) { this.toast.warn('ID y nombre son requeridos'); return; }
    this.submitting = true;
    const onSuccess = () => {
      this.toast.success(this.editMode ? 'Asesor actualizado' : 'Asesor registrado');
      this.showModal = false; this.submitting = false; this.cargarAsesores();
    };
    const onError = (e: any) => { this.toast.error(e.message); this.submitting = false; };
    if (this.editMode) {
      this.asesorService.actualizar(this.form.id, this.form).subscribe({ next: onSuccess, error: onError });
    } else {
      this.asesorService.registrar(this.form).subscribe({ next: () => onSuccess(), error: onError });
    }
  }

  private emptyForm(): AsesorDTO { return { id:'', nombre:'', contacto:'', especialidadZona:'' }; }
}
