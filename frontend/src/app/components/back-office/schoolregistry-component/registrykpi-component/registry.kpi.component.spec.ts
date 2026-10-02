import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistryKpiComponent } from './registry.kpi.component';

describe('RegistryKpiComponent', () => {
  let component: RegistryKpiComponent;
  let fixture: ComponentFixture<RegistryKpiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistryKpiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistryKpiComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
