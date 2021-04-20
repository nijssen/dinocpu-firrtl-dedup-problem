// Main entry point for CPU
package dinocpu

import chisel3._

import chisel3.stage.ChiselStage
import chisel3.experimental.{annotate, ChiselAnnotation, RunFirrtlTransform}
import firrtl.transforms.MustDeduplicateAnnotation

class Top(val conf: CPUConfig) extends Module
{
  val io = IO(new Bundle{
    val success = Output(Bool())
  })

  io.success := DontCare

  val cpus = (0 until 3).map { _ =>

    val cpu  = Module(conf.getCPU())
    val mem  = Module(conf.getNewMem())

    val imem = Module(conf.getIMemPort())
    val dmem = Module(conf.getDMemPort())

    mem.wireMemory (imem, dmem)
    cpu.io.imem <> imem.io.pipeline
    cpu.io.dmem <> dmem.io.pipeline

    cpu.toTarget
  }

  java.lang.System.out.println(cpus)

  annotate(new ChiselAnnotation with RunFirrtlTransform {
    override def toFirrtl = MustDeduplicateAnnotation(cpus)
    override def transformClass = classOf[firrtl.transforms.MustDeduplicateTransform]
  })
}
